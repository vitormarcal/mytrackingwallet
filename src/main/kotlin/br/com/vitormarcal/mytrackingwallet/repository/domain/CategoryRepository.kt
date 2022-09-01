package br.com.vitormarcal.mytrackingwallet.repository.domain

import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository: JpaRepository<Category, Long> {
}