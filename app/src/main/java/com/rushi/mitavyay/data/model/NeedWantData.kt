package com.rushi.mitavyay.data.model

/**
 * Aggregated spending data broken down by Need vs Want classification.
 */
data class NeedWantData(
    val needTotalPaise: Long = 0L,
    val wantTotalPaise: Long = 0L,
    val needPercentage: Float = 0f,
    val wantPercentage: Float = 0f,
    val needCount: Int = 0,
    val wantCount: Int = 0
) {
    val totalExpensePaise: Long get() = needTotalPaise + wantTotalPaise
}
