package br.com.vitormarcal.mytrackingwallet.controller

import java.math.BigDecimal
import java.time.LocalDate

data class MovementDTO(
        val name: String,
        val description: String? = null,
        val value: BigDecimal,
        val numberOfInstallments: Int = 0,
        val movementedAt: LocalDate = LocalDate.now(),
        val settleDate: LocalDate = LocalDate.now(),
        val credit: Boolean,
        val categoryId: Long,
)
