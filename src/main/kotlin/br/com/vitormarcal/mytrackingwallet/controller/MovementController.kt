package br.com.vitormarcal.mytrackingwallet.controller

import br.com.vitormarcal.mytrackingwallet.repository.domain.Movement
import br.com.vitormarcal.mytrackingwallet.repository.domain.MovementRepository
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

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
                    list.add(Movement(name = "${movement.name} ($installmentNumber/${movement.numberOfInstallments})",
                            description = movement.description,
                            settleDate = settleDate,
                            movementedAt = movement.movementedAt,
                            categoryId = movement.categoryId,
                            credit = movement.credit,
                            value = movement.value,
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate) =
            movementRepository.groupSumValuesByCategoryInSettleDatePeriod(startDate = startDate, endDate = endDate)
}
