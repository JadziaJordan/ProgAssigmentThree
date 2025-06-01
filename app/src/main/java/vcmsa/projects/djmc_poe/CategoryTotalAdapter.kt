package vcmsa.projects.djmc_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryTotalAdapter(private val totals: List<CategoryTotal>) :
    RecyclerView.Adapter<CategoryTotalAdapter.TotalViewHolder>() {

    class TotalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val category: TextView = itemView.findViewById(R.id.textViewCategory)
        val total: TextView = itemView.findViewById(R.id.textViewTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_total, parent, false)
        return TotalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TotalViewHolder, position: Int) {
        val item = totals[position]
        holder.category.text = item.category
        holder.total.text = "R${item.total}"
    }

    override fun getItemCount(): Int = totals.size
}
