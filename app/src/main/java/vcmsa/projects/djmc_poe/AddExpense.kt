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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private lateinit var spinnerCategory: Spinner
    private lateinit var expenseImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnCaptureImage: Button
    private lateinit var btnSave: Button

    private val categoryList = mutableListOf<String>()
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
        expenseImage = findViewById(R.id.ExpenseImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnCaptureImage = findViewById(R.id.btnCaptureImage)
        btnSave = findViewById(R.id.buttonsave)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        requestCameraPermission()
        loadCategories()

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnCaptureImage.setOnClickListener {
            val imageDir = File(filesDir, "images").apply { mkdirs() }
            val file = File(imageDir, "IMG_${System.currentTimeMillis()}.jpg")

            savedImageUri = FileProvider.getUriForFile(
                this,
                "vcmsa.projects.djmc_poe.fileprovider", // Hardcoded to match manifest
                file
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, savedImageUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, IMAGE_CAPTURE_CODE)
        }

        expenseDate.setOnClickListener {
            showDatePicker()
        }

        btnSave.setOnClickListener {
            saveExpense()
        }

        findViewById<Button>(R.id.GoalsNav).setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java)); finish()
        }
        findViewById<Button>(R.id.FinancialNav).setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java)); finish()
        }
        findViewById<Button>(R.id.DebtNav).setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java)); finish()
        }
        findViewById<Button>(R.id.GraphesNav).setOnClickListener {
            startActivity(Intent(this, Graph::class.java)); finish()
        }
    }

    private fun requestCameraPermission() {
        val permission = android.Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 200)
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
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

    private fun loadCategories() {
        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                categoryList.clear()
                snapshot.forEach { doc ->
                    doc.getString("name")?.let { categoryList.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                spinnerCategory.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveExpense() {
        val title = expenseTitle.text.toString().trim()
        val amountText = expenseAmount.text.toString().trim()
        val date = expenseDate.text.toString().trim()
        val description = expenseDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()

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
        imageUrl?.let { expense["imageUrl"] = it }

        db.collection("users").document(user.uid).collection("expenses")
            .add(expense)
            .addOnSuccessListener {
                Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ViewAllExpenses::class.java)); finish()
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
                data?.data?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    saveImageToInternalStorage(bitmap)
                    expenseImage.setImageBitmap(bitmap)
                }
            }

            IMAGE_CAPTURE_CODE -> {
                savedImageUri?.let { uri ->
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    expenseImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val imageDir = File(filesDir, "images").apply { mkdirs() }
        val file = File(imageDir, "IMG_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        savedImageUri = FileProvider.getUriForFile(
            this,
            "vcmsa.projects.djmc_poe.fileprovider",
            file
        )
    }
}
