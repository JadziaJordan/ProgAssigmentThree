package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddGoals : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var monthEditText: EditText
    private lateinit var minGoalEditText: EditText
    private lateinit var maxGoalEditText: EditText
    private lateinit var saveButton: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val calendar = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goals)

        categorySpinner = findViewById(R.id.spinnerCategory)
        monthEditText = findViewById(R.id.editTextMonth)
        minGoalEditText = findViewById(R.id.editTextMinGoal)
        maxGoalEditText = findViewById(R.id.editTextMaxGoal)
        saveButton = findViewById(R.id.buttonSaveGoal)


        loadCategories()


        val backButton = findViewById<Button>(R.id.backBtn)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        monthEditText.setOnClickListener {
            showMonthPicker()
        }

        saveButton.setOnClickListener {
            saveGoalsForMonthAndCategoryFromUI()
        }

        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)

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
    }

    private fun loadCategories() {
        val user = auth.currentUser ?: return

        db.collection("users")
            .document(user.uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = mutableListOf<String>()
                for (doc in snapshot) {
                    val name = doc.getString("name")
                    if (name != null) categories.add(name)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }

    }

    private fun showMonthPicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, _ ->
            val formatted = String.format("%04d-%02d", year, month + 1)
            monthEditText.setText(formatted)
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

   fun drawBarChart(data: List<GraphData>) {

       data.forEach {
           println("Drawing: ${it.category} | Total: ${it.total}, MinGoal: ${it.minGoal}, MaxGoal: ${it.maxGoal}")
       }
   }


    private fun saveGoalsForMonthAndCategoryFromUI() {
        val month = monthEditText.text.toString().trim()
        val category = categorySpinner.selectedItem.toString()
        val minGoal = minGoalEditText.text.toString().trim().toDoubleOrNull()
        val maxGoal = maxGoalEditText.text.toString().trim().toDoubleOrNull()

        if (month.isEmpty() || minGoal == null || maxGoal == null) {
            Toast.makeText(this, "Please enter all fields correctly", Toast.LENGTH_SHORT).show()
            return
        }

        saveGoalsForMonthAndCategory(month, category, minGoal, maxGoal)
    }

    private fun saveGoalsForMonthAndCategory(
        month: String,
        category: String,
        minGoal: Double,
        maxGoal: Double
    ) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            return
        }

        val monthDocRef = db.collection("users")
            .document(userId)
            .collection("categoryGoals")
            .document(month)

        monthDocRef.get().addOnSuccessListener { doc ->
            val updatedGoals = doc.get("goals") as? MutableMap<String, Map<String, Double>> ?: mutableMapOf()

            updatedGoals[category] = mapOf(
                "minGoal" to minGoal,
                "maxGoal" to maxGoal
            )

            monthDocRef.set(mapOf("goals" to updatedGoals))
                .addOnSuccessListener {
                    Toast.makeText(this, "Goals saved for $category, $month", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving goals: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Error retrieving goals document", Toast.LENGTH_LONG).show()
        }

    }
    fun fetchGoalsForMonth(month: String, callback: (Map<String, Goal>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val monthDocRef = db.collection("users")
            .document(userId)
            .collection("categoryGoals")
            .document(month)

        monthDocRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val goalsMap = doc.get("goals") as? Map<String, Map<String, Double>> ?: emptyMap()
                val goals = goalsMap.map { (category, values) ->
                    category to Goal(
                        category = category,
                        minGoal = values["minGoal"] ?: 0.0,
                        maxGoal = values["maxGoal"] ?: 0.0
                    )
                }.toMap()
                callback(goals)
            } else {
                callback(emptyMap())
            }
        }.addOnFailureListener {
            callback(emptyMap())
        }
    }

    fun loadChartDataForMonth(month: String) {
        val userId = auth.currentUser?.uid ?: return

        // Step 1: Fetch goals
        fetchGoalsForMonth(month) { goalsMap ->
            // Step 2: Fetch expenses
            db.collection("users")
                .document(userId)
                .collection("expenses")
                .whereGreaterThanOrEqualTo("date", "$month-01")
                .whereLessThanOrEqualTo("date", "$month-31")
                .get()
                .addOnSuccessListener { snapshot ->
                    val expenseSums = mutableMapOf<String, Double>()
                    for (doc in snapshot) {
                        val category = doc.getString("category") ?: continue
                        val amount = doc.getDouble("amount") ?: 0.0
                        expenseSums[category] = expenseSums.getOrDefault(category, 0.0) + amount
                    }

                    // Step 3: Build chart data
                    val chartData = mutableListOf<GraphData>()
                    for ((category, total) in expenseSums) {
                        val goal = goalsMap[category]
                        chartData.add(
                            GraphData(
                                category = category,
                                total = total,
                                minGoal = goal?.minGoal ?: 0.0,
                                maxGoal = goal?.maxGoal ?: 0.0
                            )
                        )
                    }

                    // Step 4: Draw bar chart
                    drawBarChart(chartData)
                }
        }
    }



}
