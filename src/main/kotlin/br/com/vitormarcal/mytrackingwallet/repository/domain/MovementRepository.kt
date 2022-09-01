package br.com.vitormarcal.mytrackingwallet.repository.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal
import java.time.LocalDate

interface MovementRepository: JpaRepository<Movement, Long> {


    @Query(
            """
                SELECT m.categoryId as categoryId,c.name AS categoryName , SUM(m.value) as totalValue FROM Movement m
                JOIN Category c ON c.id = m.categoryId
                WHERE 0=0
                AND m.settleDate between :startDate AND :endDate
                GROUP BY m.categoryId, c.name
            """
    )
    fun groupSumValuesByCategoryInSettleDatePeriod(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): List<GroupedValuesByCategoryDTO>


}

interface  GroupedValuesByCategoryDTO {
    fun getCategoryName(): String
    fun getCategoryId(): Long
    fun getTotalValue(): BigDecimal
}
