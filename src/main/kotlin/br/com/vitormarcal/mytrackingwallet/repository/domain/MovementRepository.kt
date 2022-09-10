package br.com.vitormarcal.mytrackingwallet.repository.domain

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface MovementRepository : JpaRepository<Movement, Long> {
    fun findBySettleDateBetween(startDate: LocalDate, endDate: LocalDate): List<Movement>

}
