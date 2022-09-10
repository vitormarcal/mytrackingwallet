package br.com.vitormarcal.mytrackingwallet.controller

import br.com.vitormarcal.mytrackingwallet.repository.domain.CategoryRepository
import br.com.vitormarcal.mytrackingwallet.repository.domain.Movement
import br.com.vitormarcal.mytrackingwallet.repository.domain.MovementRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/movement")
class MovementController(
        private val movementRepository: MovementRepository,
        private val categoryRepository: CategoryRepository
) {
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

    @GetMapping("consolidated")
    fun consolidated(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate): ResultValues {
        movementRepository.findBySettleDateBetween(startDate = startDate, endDate = endDate)
                .let { movements ->
                    val categories = movements.map { it.categoryId }.distinct().takeIf { it.isNotEmpty() }?.let {
                        categoryRepository.findAllById(it)
                    } ?: emptyList()
                    val diffSpentAndCredit = movements.diffSpentAndCredit()

                    val diffByCategory = movements.groupBy { it.categoryId }.map { it.key to it.value.diffSpentAndCredit() }

                    val map = diffByCategory.map { v -> categories.first { it.id == v.first }.name to v.second }.toList()

                    return ResultValues(
                            totalCredit = diffSpentAndCredit.totalCredit,
                            totalSpent = diffSpentAndCredit.totalSpent,
                            diffSpentAndCredit = diffSpentAndCredit.diffSpentAndCredit,
                            data = map
                    )

                }

    }


    fun List<Movement>.total(): BigDecimal {
        return this.map(Movement::value)
                .fold(BigDecimal.ZERO, BigDecimal::add)
    }

    fun List<Movement>.diffSpentAndCredit(): Values {
        this.groupBy { it.credit }.let { grouped ->
            val creditMovementList = grouped[true] ?: emptyList()
            val spentMovementList = grouped[false] ?: emptyList()
            val totalSpent = spentMovementList
                    .total()
            val totalCredit = creditMovementList
                    .total()
            val diffSpentAndCredit = totalSpent.add(totalCredit * "-1".toBigDecimal())
            return Values(
                    totalSpent = totalSpent,
                    totalCredit = totalCredit,
                    diffSpentAndCredit = diffSpentAndCredit,
                    creditMovementList = creditMovementList,
                    spentMovementList = spentMovementList
            )
        }
    }


    class ResultValues(
            val totalSpent: BigDecimal,
            val totalCredit: BigDecimal,
            val diffSpentAndCredit: BigDecimal,
            val data: List<Pair<String, Values>>
    )

    class Values(
            val totalSpent: BigDecimal,
            val totalCredit: BigDecimal,
            val creditMovementList: List<Movement>,
            val spentMovementList: List<Movement>,
            val diffSpentAndCredit: BigDecimal
    )

}
