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
       // TODO: Replace this with actual charting logic
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
1
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
                // Map to your Goal data class
                val goals = goalsMap.mapValues { (_, v) ->
                    Goal(
                        minGoal = v["minGoal"] ?: 0.0,
                        maxGoal = v["maxGoal"] ?: 0.0
                    )
                }
                callback(goals)
            } else {
                callback(emptyMap())
            }
        }.addOnFailureListener {
            callback(emptyMap())
        }


    }
    fun loadChartDataForMonth(month: String) {
        fun fetchExpenseTotalsByMonth(month: String, callback: (Map<String, Double>) -> Unit) {
            val userId = auth.currentUser?.uid ?: return

            val monthDocRef = db.collection("users")
                .document(userId)
                .collection("expenses")
                .document(month)

            monthDocRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val totalsMap = doc.data as? Map<String, Double> ?: emptyMap()
                    callback(totalsMap)
                } else {
                    callback(emptyMap())
                }
            }.addOnFailureListener {
                callback(emptyMap())
            }
        }

    }


}
