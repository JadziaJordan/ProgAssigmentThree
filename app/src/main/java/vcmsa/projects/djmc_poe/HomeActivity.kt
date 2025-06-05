package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnViewExpenses = findViewById<Button>(R.id.buttonViewExpenses)
        val btnDebtTracking = findViewById<Button>(R.id.buttonDebtTracking)
        val btnViewCategories = findViewById<Button>(R.id.buttonViewCategories)
        val btnAddGoals = findViewById<Button>(R.id.buttonAddGoals)


        btnViewExpenses.setOnClickListener {
            startActivity(Intent(this, ViewAllExpenses::class.java))
        }

        btnViewCategories.setOnClickListener {
            startActivity(Intent(this, ViewAllCategories::class.java))
        }

        btnDebtTracking.setOnClickListener {
            startActivity(Intent(this, DebtTrackingActivity::class.java))
        }

        btnAddGoals.setOnClickListener {
            startActivity(Intent(this, AddGoals::class.java))
        }
    }

}
