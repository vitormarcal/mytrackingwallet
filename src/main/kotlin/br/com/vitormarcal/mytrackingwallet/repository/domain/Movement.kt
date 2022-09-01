package br.com.vitormarcal.mytrackingwallet.repository.domain

import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name ="movement")
data class Movement(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String,
        val description: String? = null,
        val value: BigDecimal,
        val installmentNumber: Int? = null,
        val numberOfInstallments: Int? = null,
        val movementedAt: LocalDate = LocalDate.now(),
        val settleDate: LocalDate = LocalDate.now(),
        val credit: Boolean,
        val categoryId: Long,
        val hash: String? = null,
)
