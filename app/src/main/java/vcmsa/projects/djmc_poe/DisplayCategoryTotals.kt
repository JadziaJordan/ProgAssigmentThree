package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DisplayCategoryTotals : AppCompatActivity() {

    private lateinit var buttonPickStart: Button
    private lateinit var buttonPickEnd: Button
    private lateinit var textSelectedRange: TextView
    private lateinit var recyclerView: RecyclerView



    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var startDate: Date? = null
    private var endDate: Date? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_category_totals)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        if (auth.currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_LONG).show()
            finish() // Close activity if no user is logged in
            return
        }

        // View bindings
        buttonPickStart = findViewById(R.id.buttonPickStartDate)
        buttonPickEnd = findViewById(R.id.buttonPickEndDate)
        textSelectedRange = findViewById(R.id.textSelectedRange)
        recyclerView = findViewById(R.id.recyclerViewTotals)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Date picker listeners
        buttonPickStart.setOnClickListener { pickDate(true) }
        buttonPickEnd.setOnClickListener { pickDate(false) }



        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)

        goalsNav.setOnClickListener {
            startActivity(Intent(this, ViewAllGoalsActivity::class.java))
        }

        financialNav.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
        }

        DebtNav.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
        }

        //GraphesNav.setOnClickListener {
           // startActivity(Intent(this, GraphActivity::class.java))
        //}

    }

    private fun pickDate(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this,
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day, 0, 0, 0)
                if (isStart) {
                    startDate = selected.time
                } else {
                    endDate = selected.time
                }
                updateDateDisplay()
                if (startDate != null && endDate != null) {
                    loadTotals()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val startStr = startDate?.let { dateFormat.format(it) } ?: "?"
        val endStr = endDate?.let { dateFormat.format(it) } ?: "?"
        textSelectedRange.text = "From $startStr to $endStr"
    }

    private fun loadTotals() {
        val user = auth.currentUser ?: return
        val startStr = dateFormat.format(startDate!!)
        val endStr = dateFormat.format(endDate!!)

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .whereGreaterThanOrEqualTo("date", startStr)
            .whereLessThanOrEqualTo("date", endStr)
            .get()
            .addOnSuccessListener { snapshot ->
                val totals = HashMap<String, Double>()
                for (doc in snapshot) {
                    val category = doc.getString("category") ?: "Uncategorized"
                    val amount = doc.getDouble("amount") ?: 0.0
                    totals[category] = totals.getOrDefault(category, 0.0) + amount
                }

                val totalList = totals.map { CategoryTotal(it.key, it.value) }
                recyclerView.adapter = CategoryTotalAdapter(totalList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load totals: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}