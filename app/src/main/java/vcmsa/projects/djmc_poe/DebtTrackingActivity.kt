package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.djmc_poe.R.id.viewDebtBtn

class DebtTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_tracking)

        // Button navigation
        val categoryButton = findViewById<Button>(R.id.categoriesBtn)
        val detailButton = findViewById<Button>(R.id.detailBtn)
        val paymenthistorybutton = findViewById<Button>(R.id.paymentHistoryBtn)
        val viewdebtbutton = findViewById<Button>(viewDebtBtn)

        categoryButton.setOnClickListener {
            startActivity(Intent(this, DebtCategoryActivity::class.java))
            finish()
        }

        detailButton.setOnClickListener {
            startActivity(Intent(this, DebtDetailsActivity::class.java))
            finish()
        }

        paymenthistorybutton.setOnClickListener {
            startActivity(Intent(this, PaymentHistoryActivity::class.java))
            finish()
        }

        viewdebtbutton.setOnClickListener {
            startActivity(Intent(this, ViewDebtActivity::class.java))
            finish()
        }
    }
}
