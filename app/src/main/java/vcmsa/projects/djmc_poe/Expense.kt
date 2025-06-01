package vcmsa.projects.djmc_poe

data class Expense(
    val title: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val description: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = 0L
)
