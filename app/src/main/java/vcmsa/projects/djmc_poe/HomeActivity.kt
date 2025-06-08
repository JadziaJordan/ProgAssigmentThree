package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Bottom Navigation
        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val GraphesNav: Button = findViewById(R.id.GraphesNav)
        val DebtNav: Button = findViewById(R.id.DebtNav)

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

        val logoutButton = findViewById<Button>(R.id.logoutBtn)

        logoutButton.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

}
