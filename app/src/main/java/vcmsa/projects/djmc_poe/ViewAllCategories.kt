package vcmsa.projects.djmc_poe

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ViewAllCategories : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val categoryList = ArrayList<String>()
    private lateinit var btnAddCategory: Button
    private lateinit var btnBackHome: Button
    private lateinit var btnCategoryTotals: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_categories)

        // Initialize UI components first
        recyclerView = findViewById(R.id.recyclerViewCategories)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        btnCategoryTotals = findViewById(R.id.btnCategoryTotals)
        btnBackHome = findViewById(R.id.btnBackHome)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(categoryList)
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //navigation to add category
        btnAddCategory.setOnClickListener {
            startActivity(Intent(this, AddCategory::class.java))
        }

        //navigation to view category totals
        btnCategoryTotals.setOnClickListener {
            startActivity(Intent(this, DisplayCategoryTotals::class.java))
        }

        //navigation back to home
        btnBackHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        loadCategories()
    }
    private fun loadCategories() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(user.uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                categoryList.clear()
                for (document in documents) {
                    val name = document.getString("name")
                    if (name != null) {
                        categoryList.add(name)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}