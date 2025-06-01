package vcmsa.projects.djmc_poe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PaymentCalculatorActivity : AppCompatActivity() {

    private lateinit var totalAmountInput: EditText
    private lateinit var interestRateInput: EditText
    private lateinit var monthsInput: EditText
    private lateinit var calculateButton: Button
    private lateinit var resetButton: Button
    private lateinit var resultMonthlyPayment: TextView
    private lateinit var resultTotalPayment: TextView
    private lateinit var resultTotalInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_calculator)

        totalAmountInput = findViewById(R.id.totalAmountInput)
        interestRateInput = findViewById(R.id.interestRateInput)
        monthsInput = findViewById(R.id.monthsInput)
        calculateButton = findViewById(R.id.calculateButton)
        resetButton = findViewById(R.id.resetButton)
        resultMonthlyPayment = findViewById(R.id.resultMonthlyPayment)
        resultTotalPayment = findViewById(R.id.resultTotalPayment)
        resultTotalInterest = findViewById(R.id.resultTotalInterest)

        calculateButton.setOnClickListener {
            calculateMonthlyPayment()
        }

        resetButton.setOnClickListener {
            clearInputs()
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

        resultMonthlyPayment.text = "Monthly Payment: R${"%.2f".format(monthlyPayment)}"
        resultTotalPayment.text = "Total Payment: R${"%.2f".format(totalPayment)}"
        resultTotalInterest.text = "Total Interest: R${"%.2f".format(totalInterest)}"
    }

    private fun clearInputs() {
        totalAmountInput.text.clear()
        interestRateInput.text.clear()
        monthsInput.text.clear()
        resultMonthlyPayment.text = ""
        resultTotalPayment.text = ""
        resultTotalInterest.text = ""
    }
}
