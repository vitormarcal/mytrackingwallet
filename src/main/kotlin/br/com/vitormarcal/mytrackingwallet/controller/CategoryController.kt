package br.com.vitormarcal.mytrackingwallet.controller

import br.com.vitormarcal.mytrackingwallet.repository.domain.Category
import br.com.vitormarcal.mytrackingwallet.repository.domain.CategoryRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/category")
class CategoryController(
        private val categoryRepository: CategoryRepository
) {


    @GetMapping
    fun findAll(): List<Category> = categoryRepository.findAll()

    @PostMapping
    fun save(@RequestBody category: Category): Category = categoryRepository.save(category.copy(id = null))

}