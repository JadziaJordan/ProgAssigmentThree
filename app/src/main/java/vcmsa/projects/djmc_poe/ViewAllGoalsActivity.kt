package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ViewAllGoalsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalsAdapter
    private val goalsList = mutableListOf<GoalDisplayItem>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_goals)

        recyclerView = findViewById(R.id.recyclerViewGoals)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GoalsAdapter(goalsList)
        recyclerView.adapter = adapter

        val fromDate = findViewById<EditText>(R.id.editTextFromDate)
        val toDate = findViewById<EditText>(R.id.editTextToDate)
        val filterCategory = findViewById<Spinner>(R.id.spinnerFilterCategory)

        val addGoalBtn = findViewById<Button>(R.id.buttonAddGoal)
        addGoalBtn.setOnClickListener {
            startActivity(Intent(this, AddGoals::class.java))
        }

        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)
        val home = findViewById<Button>(R.id.HomeNav)

        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

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

        // Setup Date Pickers
        setupDatePicker(fromDate)
        setupDatePicker(toDate)

        // Load categories into spinner
        loadCategories(filterCategory)

        // Load all goals initially
        loadGoals()

        findViewById<Button>(R.id.buttonApplyFilters).setOnClickListener {
            applyFilters(fromDate.text.toString(), toDate.text.toString(), filterCategory.selectedItem.toString())
        }
    }

    private fun setupDatePicker(editText: EditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, _ ->
                val formatted = String.format("%04d-%02d", year, month + 1)
                editText.setText(formatted)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun loadCategories(spinner: Spinner) {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("categories")
            .get().addOnSuccessListener { snapshot ->
                val categories = mutableListOf("All")
                for (doc in snapshot) {
                    doc.getString("name")?.let { categories.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
    }

    private fun loadGoals() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("categoryGoals")
            .get().addOnSuccessListener { snapshot ->
                goalsList.clear()
                for (doc in snapshot) {
                    val month = doc.id
                    val goalsMap = doc.get("goals") as? Map<String, Map<String, Double>> ?: continue
                    for ((category, data) in goalsMap) {
                        goalsList.add(GoalDisplayItem(month, category, data["minGoal"] ?: 0.0, data["maxGoal"] ?: 0.0))
                    }
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun applyFilters(from: String, to: String, category: String) {
        val filtered = goalsList.filter {
            val inRange = it.month >= from && it.month <= to
            val matchCategory = category == "All" || it.category == category
            inRange && matchCategory
        }
        adapter.updateData(filtered)
    }
}

data class GoalDisplayItem(val month: String, val category: String, val minGoal: Double, val maxGoal: Double)
