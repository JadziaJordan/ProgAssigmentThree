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
import vcmsa.projects.djmc_poe.R.id.recyclerView
import vcmsa.projects.djmc_poe.model.Payment

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentAdapter: PaymentAdapter
    private val paymentList = mutableListOf<Payment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        paymentAdapter = PaymentAdapter(paymentList)
        recyclerView.adapter = paymentAdapter

        fetchPaymentData()

        val backButton = findViewById<Button>(R.id.backBtn)
        val expensenav = findViewById<Button>(R.id.FinancialNav)
        val graphsnav = findViewById<Button>(R.id.GraphesNav)
        val debtnav = findViewById<Button>(R.id.DebtNav)
        val categoriesnav = findViewById<Button>(R.id.CatNav)

        expensenav.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
            finish()
        }

        graphsnav.setOnClickListener {
            startActivity(Intent(this, ViewAllGoalsActivity::class.java))
            finish()
        }

        debtnav.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }

        categoriesnav.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
            finish()
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }
    }

    private fun fetchPaymentData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val paymentRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("payments")

        paymentRef.get()
            .addOnSuccessListener { snapshot ->
                paymentList.clear()
                for (doc in snapshot) {
                    val payment = doc.toObject(Payment::class.java).copy(documentId = doc.id)
                    paymentList.add(payment)
                }
                paymentAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load payment data", Toast.LENGTH_SHORT).show()
            }
    }
}

