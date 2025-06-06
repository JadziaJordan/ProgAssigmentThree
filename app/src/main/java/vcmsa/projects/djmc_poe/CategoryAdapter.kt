package vcmsa.projects.djmc_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categoryList: List<Category>,
    private val onLongClick: (Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.textViewCategoryName)
        val categoryDescriptionTextView: TextView = itemView.findViewById(R.id.textViewCategoryDescription)

        init {
            itemView.setOnLongClickListener {
                onLongClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryNameTextView.text = category.name
        holder.categoryDescriptionTextView.text = category.description
    }

    override fun getItemCount(): Int = categoryList.size
}
