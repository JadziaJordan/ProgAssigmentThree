package vcmsa.projects.djmc_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.djmc_poe.model.Payment
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore

class PaymentAdapter(private val paymentList: List<Payment>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountText: TextView = itemView.findViewById(R.id.textPaymentAmount)
        val dateText: TextView = itemView.findViewById(R.id.textPaymentDate)
        val debtNameText: TextView = itemView.findViewById(R.id.textDebtName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = paymentList[position]
        holder.amountText.text = "R${payment.amount}"

        // Format timestamp if not null
        val formattedDate = payment.timestamp?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it)
        } ?: "N/A"

        holder.dateText.text = formattedDate

        // Set a default debt name while loading
        holder.debtNameText.text = "Loading..."

        // Fetch debt name using debtId
        db.collection("debts").document(payment.debtId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val debtName = document.getString("name") ?: "Unknown Debt"
                    holder.debtNameText.text = debtName
                } else {
                    holder.debtNameText.text = "Unknown Debt"
                }
            }
            .addOnFailureListener {
                holder.debtNameText.text = "Failed to load"
            }

    }

    override fun getItemCount(): Int {
        return paymentList.size
    }
}
