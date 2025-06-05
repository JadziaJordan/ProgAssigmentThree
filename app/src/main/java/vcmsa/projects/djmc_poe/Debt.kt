package vcmsa.projects.djmc_poe

data class Debt(
    val debtname: String = "",
    val debtamt: Double = 0.0,
    val interestamt: Double = 0.0,
    val npmonthsperiod: Int = 0,
    val category: String = "",
    val documentId: String = ""
)
{
    val totalDebtAmount: Double
        get() = debtamt + interestamt

}