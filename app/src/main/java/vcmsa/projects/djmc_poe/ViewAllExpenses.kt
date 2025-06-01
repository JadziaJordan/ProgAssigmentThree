package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewAllExpenses : AppCompatActivity() {

    private lateinit var etDateFrom: EditText
    private lateinit var etDateTo: EditText
    private lateinit var btnFilterRange: Button
    private lateinit var btnClearFilter: Button
    private lateinit var btnAddExpense: Button // 🔹 New Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var allExpenses = listOf<Expense>() // full list loaded from Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_expenses)

        etDateFrom = findViewById(R.id.etDateFrom)
        etDateTo = findViewById(R.id.etDateTo)
        btnFilterRange = findViewById(R.id.btnFilterRange)
        btnClearFilter = findViewById(R.id.btnClearFilter)
        btnAddExpense = findViewById(R.id.btnAddExpense) // 🔹 Initialize button
        recyclerView = findViewById(R.id.recyclerViewExpenses)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(emptyList()) { expense -> deleteExpense(expense) }
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etDateFrom.setOnClickListener { showDatePicker(etDateFrom) }
        etDateTo.setOnClickListener { showDatePicker(etDateTo) }

        btnFilterRange.setOnClickListener { filterByDateRange() }
        btnClearFilter.setOnClickListener { clearFilter() }

        // 🔹 Route to AddExpense
        btnAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpense::class.java)
            startActivity(intent)
        }

        loadAllExpenses()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            editText.setText(selectedDate)
        }, year, month, day)
        datePicker.show()
    }

    private fun loadAllExpenses() {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .get()
            .addOnSuccessListener { result ->
                allExpenses = result.mapNotNull { it.toObject(Expense::class.java) }
                adapter.updateList(allExpenses)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load expenses", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterByDateRange() {
        val fromDateStr = etDateFrom.text.toString()
        val toDateStr = etDateTo.text.toString()

        if (fromDateStr.isBlank() || toDateStr.isBlank()) {
            Toast.makeText(this, "Please select both From and To dates", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fromDate = sdf.parse(fromDateStr)
            val toDate = sdf.parse(toDateStr)

            if (fromDate == null || toDate == null) {
                Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
                return
            }

            if (fromDate.after(toDate)) {
                Toast.makeText(this, "'From' date cannot be after 'To' date", Toast.LENGTH_SHORT).show()
                return
            }

            val filtered = allExpenses.filter { expense ->
                val expenseDate = try {
                    sdf.parse(expense.date)
                } catch (e: Exception) {
                    null
                }
                expenseDate != null && !expenseDate.before(fromDate) && !expenseDate.after(toDate)
            }

            adapter.updateList(filtered)

        } catch (e: Exception) {
            Toast.makeText(this, "Error filtering dates: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFilter() {
        etDateFrom.text.clear()
        etDateTo.text.clear()
        adapter.updateList(allExpenses)
    }

    private fun deleteExpense(expense: Expense) {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("expenses")
            .whereEqualTo("timestamp", expense.timestamp)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    doc.reference.delete()
                }
                loadAllExpenses()
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
