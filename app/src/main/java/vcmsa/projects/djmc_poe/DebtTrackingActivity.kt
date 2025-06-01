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
        val expenseButton = findViewById<Button>(R.id.expenseButton)
        val paymentCalculator = findViewById<Button>(R.id.paymentCalculator)

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

        expenseButton.setOnClickListener {
            val intent = Intent(this, ViewAllExpenses::class.java)
            startActivity(intent)
        }

        paymentCalculator.setOnClickListener {
            val intent = Intent(this, PaymentCalculatorActivity::class.java)
            startActivity(intent)
        }
    }
}
