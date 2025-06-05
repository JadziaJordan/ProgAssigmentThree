
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

class DebtCategoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_category)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = currentUser.uid

        // UI Components
        val debtCategoryEditText = findViewById<EditText>(R.id.etDebtCategoryname)
        val createButton = findViewById<Button>(R.id.createBtn)
        val backButton = findViewById<Button>(R.id.backBtn)

        createButton.setOnClickListener {
            val debtCategoryName = debtCategoryEditText.text.toString().trim()

            if (debtCategoryName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val debtCategory = hashMapOf(
                "debtcategoryname" to debtCategoryName
            )

            // Save to /users/{userId}/debtcategorynames
            db.collection("users").document(userId).collection("debtcategorynames")
                .add(debtCategory)
                .addOnSuccessListener { documentReference ->
                    Log.d("FIRESTORE", "Debt category saved: ${documentReference.id}")
                    Toast.makeText(this, "Debt category saved!", Toast.LENGTH_SHORT).show()
                    debtCategoryEditText.text.clear()
                }
                .addOnFailureListener { e ->
                    Log.w("FIRESTORE", "Error saving debt category", e)
                    Toast.makeText(this, "Error saving debt category", Toast.LENGTH_SHORT).show()
                }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
            finish()
        }
    }
}
