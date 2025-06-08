package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PaymentEntryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_entry)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG)
                .show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid
        val backButton = findViewById<Button>(R.id.backBtn)
        val paymentEntryButton = findViewById<Button>(R.id.PaymentEntrybtn)
        val paymentAmountInput = findViewById<EditText>(R.id.editTextNumber)
        val debtId = intent.getStringExtra("debtId")
        val expensenav = findViewById<Button>(R.id.FinancialNav)
        val graphsnav = findViewById<Button>(R.id.GraphesNav)
        val debtnav = findViewById<Button>(R.id.DebtNav)
        val categoriesnav = findViewById<Button>(R.id.CatNav)

        backButton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }

        expensenav.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
            finish()
        }

        graphsnav.setOnClickListener {
            startActivity(Intent(this, Graph::class.java))
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

//        paymentEntryButton.setOnClickListener {
//            val paymentAmount = paymentAmountInput.text.toString().toDoubleOrNull()
//
//            if (paymentAmount == null || paymentAmount <= 0) {
//                Toast.makeText(this, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (debtId == null) {
//                Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Step 1: Get the debt document
//            val debtRef = db.collection("users").document(userId)
//                .collection("debtdetails").document(debtId)
//
//            debtRef.get().addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    val currentDebtAmount = documentSnapshot.getDouble("debtamt") ?: 0.0
//                    val newDebtAmount = currentDebtAmount - paymentAmount
//
//                    // Step 2: Update debtamt in Firestore
//                    debtRef.update("debtamt", newDebtAmount)
//                        .addOnSuccessListener {
//
//
//                            debtRef.update("debtamt", newDebtAmount)
//                                .addOnSuccessListener {
//                                    // Log payment under user's payments collection
//                                    val payment = hashMapOf(
//                                        "amount" to paymentAmount,
//                                        "timestamp" to Date(),
//                                        "debtId" to debtId
//                                    )
//
//                                    db.collection("users")
//                                        .document(userId)
//                                        .collection("payments")
//                                        .add(payment)
//                                        .addOnSuccessListener {
//                                            Toast.makeText(this, "Payment saved and debt updated!", Toast.LENGTH_SHORT).show()
//                                            paymentAmountInput.text.clear()
//                                        }
//                                }
//                                .addOnFailureListener { e ->
//                                    Toast.makeText(this, "Payment saved, but failed to log payment: ${e.message}", Toast.LENGTH_LONG).show()
//                                }
//                        }
//                        .addOnFailureListener { e ->
//                            Toast.makeText(this, "Failed to update debt: ${e.message}", Toast.LENGTH_LONG).show()
//                        }
//                } else {
//                    Toast.makeText(this, "Debt record not found", Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to fetch debt: ${e.message}", Toast.LENGTH_LONG).show()
//            }

        paymentEntryButton.setOnClickListener {
            val paymentAmount = paymentAmountInput.text.toString().toDoubleOrNull()

            if (paymentAmount == null || paymentAmount <= 0) {
                Toast.makeText(this, "Enter a valid payment amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (debtId == null) {
                Toast.makeText(this, "Debt not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val debtRef = db.collection("users").document(userId)
                .collection("debtdetails").document(debtId)

            debtRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val currentDebtAmount = documentSnapshot.getDouble("debtamt") ?: 0.0
                    val currentInterest = documentSnapshot.getDouble("interestamt") ?: 0.0
                    var remainingPayment = paymentAmount
                    var newDebtAmount = currentDebtAmount
                    var newInterest = currentInterest

                    if (newDebtAmount > 0) {
                        if (remainingPayment >= newDebtAmount) {
                            remainingPayment -= newDebtAmount
                            newDebtAmount = 0.0
                        } else {
                            newDebtAmount -= remainingPayment
                            remainingPayment = 0.0
                        }
                    }

                    if (remainingPayment > 0 && newInterest > 0) {
                        if (remainingPayment >= newInterest) {
                            remainingPayment -= newInterest
                            newInterest = 0.0
                        } else {
                            newInterest -= remainingPayment
                            remainingPayment = 0.0
                        }
                    }

                    val paymentLog = hashMapOf(
                        "amount" to paymentAmount,
                        "timestamp" to Date(),
                        "debtId" to debtId
                    )

                    // Log the payment
                    db.collection("users").document(userId)
                        .collection("payments")
                        .add(paymentLog)

                    // Delete if fully paid
                    if (newDebtAmount == 0.0 && newInterest == 0.0) {
                        debtRef.delete().addOnSuccessListener {
                            Toast.makeText(this, "Debt fully paid off and deleted.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, ViewDebtActivity::class.java))
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to delete debt: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Otherwise update
                        val updates = hashMapOf<String, Any>(
                            "debtamt" to newDebtAmount,
                            "interestamt" to newInterest
                        )
                        debtRef.update(updates).addOnSuccessListener {
                            Toast.makeText(this, "Payment processed successfully!", Toast.LENGTH_SHORT).show()
                            paymentAmountInput.text.clear()
                            startActivity(Intent(this, ViewDebtActivity::class.java))
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this, "Failed to update debt: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Debt record not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to fetch debt: ${e.message}", Toast.LENGTH_LONG).show()
            }


        //redirect to view debts
            startActivity(Intent(this, ViewDebtActivity::class.java))
            finish()

        }
    }
}
