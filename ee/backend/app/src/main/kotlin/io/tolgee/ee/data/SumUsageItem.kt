package io.tolgee.ee.data

import java.math.BigDecimal

data class SumUsageItem(
  val total: BigDecimal,
  val usedQuantityOverPlan: Long,
  val unusedQuantity: Long,
  val usedQuantity: Long
)
