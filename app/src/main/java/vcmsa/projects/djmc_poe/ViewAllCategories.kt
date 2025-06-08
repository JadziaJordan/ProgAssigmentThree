package vcmsa.projects.djmc_poe

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// References
//    >https://stackoverflow.com/questions/61092782/understanding-firestore-queries

class ViewAllCategories : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val categoryList = ArrayList<Category>()
    private val documentIdList = ArrayList<String>()

    private lateinit var btnAddCategory: Button
    private lateinit var btnCategoryTotals: Button
    private lateinit var btnViewGoals: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_categories)

        recyclerView = findViewById(R.id.recyclerViewCategories)
        btnAddCategory = findViewById(R.id.btnAddCategory)
        btnCategoryTotals = findViewById(R.id.btnCategoryTotals)
        btnViewGoals = findViewById(R.id.btnViewGoals)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter(categoryList) { position -> showDeleteDialog(position) }
        recyclerView.adapter = adapter

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        btnAddCategory.setOnClickListener {
            startActivity(Intent(this, AddCategory::class.java))
        }

        btnCategoryTotals.setOnClickListener {
            startActivity(Intent(this, DisplayCategoryTotals::class.java))
        }

        btnViewGoals.setOnClickListener {
            startActivity(Intent(this, ViewAllGoalsActivity::class.java))
        }

        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)
        val home = findViewById<Button>(R.id.HomeNav)

        goalsNav.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
        }

        financialNav.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
        }

        DebtNav.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
        }

        GraphesNav.setOnClickListener {
            startActivity(Intent(this, Graph::class.java))
        }

        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        loadCategories()
    }


    private fun loadCategories() {
        val user = auth.currentUser ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(user.uid)
            .collection("categories")
            .get()
            .addOnSuccessListener { documents ->
                categoryList.clear()
                documentIdList.clear()
                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    categoryList.add(Category(name, description))
                    documentIdList.add(document.id)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteDialog(position: Int) {
        val category = categoryList[position]
        val categoryId = documentIdList[position]

        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete \"${category.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(categoryId, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(documentId: String, position: Int) {
        val user = auth.currentUser ?: return

        db.collection("users")
            .document(user.uid)
            .collection("categories")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                categoryList.removeAt(position)
                documentIdList.removeAt(position)
                adapter.notifyItemRemoved(position)
                Toast.makeText(this, "Category deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
