package br.com.vitormarcal.mytrackingwallet.controller

import br.com.vitormarcal.mytrackingwallet.repository.domain.GroupedValuesByCategoryDTO
import br.com.vitormarcal.mytrackingwallet.repository.domain.Movement
import br.com.vitormarcal.mytrackingwallet.repository.domain.MovementRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/movement")
class MovementController(private val movementRepository: MovementRepository) {
    @GetMapping
    fun findAll(): List<Movement> = movementRepository.findAll()

    @PostMapping
    fun save(@RequestBody movement: MovementDTO): List<Movement> {
        val hash = UUID.randomUUID().toString()
        if (movement.numberOfInstallments > 1) {
            mutableListOf<Movement>().let { list ->
                repeat(movement.numberOfInstallments) {
                    val installmentNumber = it + 1
                    val settleDate = list.lastOrNull()?.settleDate?.plusMonths(1) ?: movement.settleDate
                    val monthlyValue = movement.value.divide(movement.numberOfInstallments.toBigDecimal().setScale(2), RoundingMode.HALF_UP).setScale(2)
                    list.add(Movement(name = "${movement.name} ($installmentNumber/${movement.numberOfInstallments})",
                            description = movement.description,
                            settleDate = settleDate,
                            movementedAt = movement.movementedAt,
                            categoryId = movement.categoryId,
                            credit = movement.credit,
                            value = monthlyValue,
                            numberOfInstallments = movement.numberOfInstallments,
                            installmentNumber = installmentNumber,
                            hash = hash))
                }
                return movementRepository.saveAll(list)
            }

        }
        return Movement(name = movement.name,
                description = movement.description,
                value = movement.value,
                movementedAt = movement.movementedAt,
                settleDate = movement.settleDate,
                credit = movement.credit,
                categoryId = movement.categoryId,
                hash = hash).let {
            listOf(movementRepository.save(it))
        }
    }

    @GetMapping("values")
    fun groupyValuesByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate): TotalMovement =
            movementRepository.groupSumValuesByCategoryInSettleDatePeriod(startDate = startDate, endDate = endDate).let {
                val total = it.map { movement -> movement.getTotalValue() }
                        .fold(BigDecimal.ZERO, BigDecimal::add)

                return TotalMovement(
                       total = total,
                        totalByCategory = it
                )
            }

    class TotalMovement(
            val total: BigDecimal,
            val totalByCategory: List<GroupedValuesByCategoryDTO>
    )

}
