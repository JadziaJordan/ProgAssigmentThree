package vcmsa.projects.djmc_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoalsAdapter(private var items: List<GoalDisplayItem>) :
    RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    inner class GoalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val month = view.findViewById<TextView>(R.id.textViewMonth)
        val category = view.findViewById<TextView>(R.id.textViewCategory)
        val minGoal = view.findViewById<TextView>(R.id.textViewMinGoal)
        val maxGoal = view.findViewById<TextView>(R.id.textViewMaxGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val item = items[position]
        holder.month.text = "Month: ${item.month}"
        holder.category.text = "Category: ${item.category}"
        holder.minGoal.text = "Min Goal: ${item.minGoal}"
        holder.maxGoal.text = "Max Goal: ${item.maxGoal}"
    }

    fun updateData(newList: List<GoalDisplayItem>) {
        items = newList
        notifyDataSetChanged()
    }
}