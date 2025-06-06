package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddCategory : AppCompatActivity() {

    private lateinit var editTextCategoryName: EditText
    private lateinit var editTextCategoryDescription: EditText
    private lateinit var btnSaveCategory: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        editTextCategoryName = findViewById(R.id.editTextCategoryName)
        editTextCategoryDescription = findViewById(R.id.editTextCategoryDescription)
        btnSaveCategory = findViewById(R.id.btnSaveCategory)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnSaveCategory.setOnClickListener {
            val name = editTextCategoryName.text.toString().trim()
            val description = editTextCategoryDescription.text.toString().trim()

            if (name.isEmpty()) {
                editTextCategoryName.error = "Name required"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                editTextCategoryDescription.error = "Description required"
                return@setOnClickListener
            }

            val category = hashMapOf(
                "name" to name,
                "description" to description
            )

            val user = auth.currentUser
            if (user != null) {
                db.collection("users")
                    .document(user.uid)
                    .collection("categories")
                    .add(category)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Category saved", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ViewAllCategories::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving category: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

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
            //startActivity(Intent(this, GraphActivity::class.java))
        //}
    }
}

