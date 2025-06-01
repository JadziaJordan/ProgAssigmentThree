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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddExpense : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var expenseTitle: EditText
    private lateinit var expenseAmount: EditText
    private lateinit var expenseDate: EditText
    private lateinit var expenseDescription: EditText
    private lateinit var expenseCategory: Spinner
    private lateinit var expenseImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnCaptureImage: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button

    private var savedImageUri: Uri? = null

    private val IMAGE_PICK_CODE = 101
    private val IMAGE_CAPTURE_CODE = 102
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        expenseTitle = findViewById(R.id.Expense)
        expenseAmount = findViewById(R.id.ExpenseAmount)
        expenseDate = findViewById(R.id.Date)
        expenseDescription = findViewById(R.id.ExpenseDescription)
        expenseCategory = findViewById(R.id.ExpenseCategory)
        expenseImage = findViewById(R.id.ExpenseImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCaptureImage = findViewById(R.id.btnCaptureImage)
        btnSave = findViewById(R.id.buttonsave)
        btnBack = findViewById(R.id.BackToReport)

        val categories = listOf("Food", "Fuel", "Transport", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        expenseCategory.adapter = adapter

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnCaptureImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, IMAGE_CAPTURE_CODE)
        }

        expenseDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveExpense()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            expenseDate.setText(format.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveExpense() {
        val title = expenseTitle.text.toString().trim()
        val amountText = expenseAmount.text.toString().trim()
        val date = expenseDate.text.toString().trim()
        val description = expenseDescription.text.toString().trim()
        val category = expenseCategory.selectedItem.toString()

        if (title.isEmpty() || amountText.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_LONG).show()
            return
        }

        val imageUriString = savedImageUri?.toString()
        Log.d("ImageURI", "Saving URI: $imageUriString")

        saveExpenseToFirestore(title, amount, date, description, category, imageUriString)
    }

    private fun saveExpenseToFirestore(
        title: String,
        amount: Double,
        date: String,
        description: String,
        category: String,
        imageUrl: String?
    ) {
        val user = auth.currentUser ?: return
        val expense = hashMapOf(
            "title" to title,
            "amount" to amount,
            "date" to date,
            "description" to description,
            "category" to category,
            "timestamp" to System.currentTimeMillis()
        )
        if (!imageUrl.isNullOrEmpty()) {
            expense["imageUrl"] = imageUrl
        }

        db.collection("users")
            .document(user.uid)
            .collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ViewAllExpenses::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error saving expense", e)
                Toast.makeText(this, "Failed to save expense: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            IMAGE_PICK_CODE -> {
                val uri = data?.data
                if (uri != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    saveImageToInternalStorage(bitmap)
                    expenseImage.setImageBitmap(bitmap)
                }
            }

            IMAGE_CAPTURE_CODE -> {
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    saveImageToInternalStorage(bitmap)
                    expenseImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val imageFile = File(filesDir, "images")
        if (!imageFile.exists()) imageFile.mkdirs()

        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(imageFile, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        savedImageUri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
    }
}
