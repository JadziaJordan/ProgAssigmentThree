package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import vcmsa.projects.djmc_poe.R.id.backBtn

class ViewDebtActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var debtAdapter: DebtAdapter
    private val debtList = mutableListOf<Debt>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_debt)

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set up adapter with delete callback
        val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        val firestore = FirebaseFirestore.getInstance()

        debtAdapter = DebtAdapter(this, debtList) { debt, position ->
            firestore.collection("users")
                .document(uid)
                .collection("debtdetails")
                .document(debt.documentId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Debt deleted", Toast.LENGTH_SHORT).show()
                    debtAdapter.removeDebt(position)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        recyclerView.adapter = debtAdapter

        // Fetch debts from Firestore
        fetchDebtData()

        //back button

        val backbutton = findViewById<Button>(backBtn)

        backbutton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }


    }

    private fun fetchDebtData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val debtRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("debtdetails")

        debtRef.get()
            .addOnSuccessListener { snapshot ->
                debtList.clear()
                for (doc in snapshot) {
                    val debt = doc.toObject(Debt::class.java).copy(documentId = doc.id)
                    debtList.add(debt)
                }
                debtAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load debt data", Toast.LENGTH_SHORT).show()
            }
    }
}