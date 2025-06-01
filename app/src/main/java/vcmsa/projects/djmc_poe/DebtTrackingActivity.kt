package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DebtTrackingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_debt_tracking)

        val categoryButton = findViewById<Button>(R.id.categoriesBtn)
        val detailButton = findViewById<Button>(R.id.detailBtn)
        val backButton = findViewById<Button>(R.id.backBtn)


        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        categoryButton.setOnClickListener {
            val intent = Intent(this, DebtCategoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        detailButton.setOnClickListener {
            val intent = Intent(this, DebtDetailsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

