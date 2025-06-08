package vcmsa.projects.djmc_poe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class PaymentCalculatorActivity : AppCompatActivity() {

// Refrences:
//    >https://stackoverflow.com/questions/61050006/consulting-about-loan-calculator-function

    private lateinit var totalAmountInput: EditText
    private lateinit var interestRateInput: EditText
    private lateinit var monthsInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resetButton: Button

    private lateinit var resultCardMonthly: CardView
    private lateinit var resultCardTotal: CardView
    private lateinit var resultCardInterest: CardView

    private lateinit var resultMonthlyPayment: TextView
    private lateinit var resultTotalPayment: TextView
    private lateinit var resultTotalInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_calculator)

        // Input fields
        totalAmountInput = findViewById(R.id.totalAmountInput)
        interestRateInput = findViewById(R.id.interestRateInput)
        monthsInput = findViewById(R.id.monthsInput)
        calculateButton = findViewById(R.id.calculateButton)
        resetButton = findViewById(R.id.resetButton)

        // Result cards (initially hidden)
        resultCardMonthly = findViewById(R.id.resultCardMonthly)
        resultCardTotal = findViewById(R.id.resultCardTotal)
        resultCardInterest = findViewById(R.id.resultCardInterest)

        // Result TextViews
        resultMonthlyPayment = findViewById(R.id.resultMonthlyPayment)
        resultTotalPayment = findViewById(R.id.resultTotalPayment)
        resultTotalInterest = findViewById(R.id.resultTotalInterest)

        // Calculate button listener
        calculateButton.setOnClickListener {
            calculateMonthlyPayment()
        }

        // Reset button listener
        resetButton.setOnClickListener {
            clearInputs()
        }

        val goalsNav: Button = findViewById(R.id.GoalsNav)
        val financialNav: Button = findViewById(R.id.FinancialNav)
        val debtNav: Button = findViewById(R.id.DebtNav)
        val graphesNav: Button = findViewById(R.id.GraphesNav)

        goalsNav.setOnClickListener {
            val intent = Intent(this, DebtTrackingActivity::class.java)
            startActivity(intent)
            finish()
        }

        financialNav.setOnClickListener {
            val intent = Intent(this, ViewAllExpenses::class.java)
            startActivity(intent)
            finish()
        }

        debtNav.setOnClickListener {
            val intent = Intent(this, ViewAllCategories::class.java)
            startActivity(intent)
            finish()
        }

        //change

        graphesNav.setOnClickListener {
            val intent = Intent(this, Graph::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun calculateMonthlyPayment() {
        val amountText = totalAmountInput.text.toString()
        val interestText = interestRateInput.text.toString()
        val monthsText = monthsInput.text.toString()

        if (amountText.isBlank() || interestText.isBlank() || monthsText.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val principal = amountText.toDoubleOrNull()
        val annualInterestRate = interestText.toDoubleOrNull()
        val numberOfMonths = monthsText.toIntOrNull()

        if (principal == null || annualInterestRate == null || numberOfMonths == null || numberOfMonths == 0) {
            Toast.makeText(this, "Enter valid numeric values", Toast.LENGTH_SHORT).show()
            return
        }

        val monthlyInterestRate = annualInterestRate / 100 / 12

        val monthlyPayment = if (monthlyInterestRate == 0.0) {
            principal / numberOfMonths
        } else {
            val factor = Math.pow(1 + monthlyInterestRate, numberOfMonths.toDouble())
            (principal * monthlyInterestRate * factor) / (factor - 1)
        }

        val totalPayment = monthlyPayment * numberOfMonths
        val totalInterest = totalPayment - principal

        // Format as “Rxx.xx”
        resultMonthlyPayment.text = "Monthly Payment: R${"%.2f".format(monthlyPayment)}"
        resultTotalPayment.text = "Total Payment: R${"%.2f".format(totalPayment)}"
        resultTotalInterest.text = "Total Interest: R${"%.2f".format(totalInterest)}"

        // Show the cards
        resultCardMonthly.visibility = View.VISIBLE
        resultCardTotal.visibility = View.VISIBLE
        resultCardInterest.visibility = View.VISIBLE
    }

    private fun clearInputs() {
        totalAmountInput.text.clear()
        interestRateInput.text.clear()
        monthsInput.text.clear()

        // Hide result cards again
        resultCardMonthly.visibility = View.GONE
        resultCardTotal.visibility = View.GONE
        resultCardInterest.visibility = View.GONE
    }
}
