package vcmsa.projects.djmc_poe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Graph : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var spinnerMonth: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        barChart = findViewById(R.id.barChart)
        spinnerMonth = findViewById(R.id.spinnerMonth)

        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)
        val home = findViewById<Button>(R.id.HomeNav)

        goalsNav.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
        }

        financialNav.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
        }

        DebtNav.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
        }

        GraphesNav.setOnClickListener {
            startActivity(Intent(this, Graph::class.java))
        }

        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.d("GraphDebug", "User not authenticated")
            return
        }

        // Dynamically load months from Firestore
        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("categoryGoals")
            .get()
            .addOnSuccessListener { snapshot ->
                val months = snapshot.documents.map { it.id }.sorted()

                if (months.isEmpty()) {
                    Log.d("GraphDebug", "No categoryGoals found")
                    return@addOnSuccessListener
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerMonth.adapter = adapter

                spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedMonth = months[position]
                        fetchGoals(selectedMonth)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
            }
            .addOnFailureListener {
                Log.d("GraphDebug", "Failed to load months: ${it.message}")
            }
    }

    private fun fetchGoals(selectedMonth: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Log.d("GraphDebug", "User not authenticated")
            return
        }

        val goalsMap = mutableMapOf<String, Goal>()
        val goalsRef = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("categoryGoals")
            .document(selectedMonth)

        goalsRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val goalsData = document.get("goals") as? Map<*, *>
                if (goalsData != null) {
                    for ((category, goalInfoAny) in goalsData) {
                        if (category is String && goalInfoAny is Map<*, *>) {
                            val minGoal = (goalInfoAny["minGoal"] as? Number)?.toDouble() ?: 0.0
                            val maxGoal = (goalInfoAny["maxGoal"] as? Number)?.toDouble() ?: 0.0
                            goalsMap[category] = Goal(category, minGoal, maxGoal)
                        }
                    }
                } else {
                    Log.d("GraphDebug", "No 'goals' field found in $selectedMonth")
                }
            } else {
                Log.d("GraphDebug", "Document does not exist: users/$userId/categoryGoals/$selectedMonth")
            }

            Log.d("GraphDebug", "Parsed goals: $goalsMap")
            fetchExpenses(goalsMap, selectedMonth, userId)

        }.addOnFailureListener {
            Log.d("GraphDebug", "Failed to fetch goals: ${it.message}")
        }
    }

    private fun fetchExpenses(goalsMap: Map<String, Goal>, selectedMonth: String, userId: String) {
        val expenseTotals = mutableMapOf<String, Double>()

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("date", "$selectedMonth-01")
            .whereLessThanOrEqualTo("date", "$selectedMonth-31")
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot) {
                    val category = doc.getString("category") ?: continue
                    val amount = doc.getDouble("amount") ?: 0.0
                    Log.d("GraphDebug", "Expense found - Category: $category, Amount: $amount")

                    expenseTotals[category] = expenseTotals.getOrDefault(category, 0.0) + amount
                }

                Log.d("GraphDebug", "Fetched expenses: $expenseTotals")
                Log.d("GraphDebug", "Final Goals: $goalsMap")
                drawBarChart(expenseTotals, goalsMap)
            }
            .addOnFailureListener {
                Log.d("GraphDebug", "Failed to fetch expenses: ${it.message}")
                drawBarChart(emptyMap(), goalsMap)
            }
    }

    private fun drawBarChart(expenseTotals: Map<String, Double>, goalsMap: Map<String, Goal>) {
        val entries = mutableListOf<BarEntry>()
        val colors = mutableListOf<Int>()
        val labels = mutableListOf<String>()
        var index = 0f

        for ((category, totalExpense) in expenseTotals) {
            entries.add(BarEntry(index, totalExpense.toFloat()))
            labels.add(category)

            val goal = goalsMap[category]
            val color = when {
                goal == null -> Color.GRAY
                totalExpense < goal.minGoal -> Color.GREEN
                totalExpense < goal.maxGoal -> Color.YELLOW
                else -> Color.RED
            }
            colors.add(color)
            index++
        }

        Log.d("GraphDebug", "Entries: $entries, Labels: $labels")

        if (entries.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("No expenses to display.")
            barChart.invalidate()
            return
        }

        val dataSet = BarDataSet(entries, "Expenses by Category")
        dataSet.colors = colors
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.invalidate()
    }
}
