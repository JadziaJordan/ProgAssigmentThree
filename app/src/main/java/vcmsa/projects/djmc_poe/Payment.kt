package vcmsa.projects.djmc_poe.model

import java.util.Date

data class Payment(
    val amount: Double = 0.0,
    val timestamp: Date? = null,
    val debtId: String = "",
    val documentId: String = ""
)
