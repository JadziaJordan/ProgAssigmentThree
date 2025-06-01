package vcmsa.projects.djmc_poe

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddCategory : AppCompatActivity() {

    private lateinit var editCategoryName: EditText
    private lateinit var buttonSaveCategory: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_category)

        editCategoryName = findViewById(R.id.editTextCategoryName)
        buttonSaveCategory = findViewById(R.id.buttonSaveCategory)
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnBackToCategories = findViewById<Button>(R.id.btnBackToCategories)

        btnBackToCategories.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
            finish()
        }

        buttonSaveCategory.setOnClickListener {
            saveCategory()
        }
    }

    private fun saveCategory() {
        val categoryName = editCategoryName.text.toString().trim()

        if (categoryName.isEmpty()) {
            editCategoryName.error = "Category name is required"
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val category = hashMapOf(
            "name" to categoryName
        )

        db.collection("users")
            .document(user.uid)
            .collection("categories")
            .add(category)
            .addOnSuccessListener {
                Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                editCategoryName.text.clear()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding category: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
    }
}
