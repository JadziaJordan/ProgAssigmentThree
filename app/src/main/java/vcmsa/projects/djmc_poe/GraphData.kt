package vcmsa.projects.djmc_poe

data class GraphData(
    val category: String,
    val total: Double,
    val minGoal: Double,
    val maxGoal: Double
)

data class Goal(
    val minGoal: Double,
    val maxGoal: Double
)

data class GoalDisplay(
    val month: String,
    val category: String,
    val minGoal: Double,
    val maxGoal: Double
)

