package br.com.vitormarcal.mytrackingwallet.repository.domain

import javax.persistence.*

@Entity
@Table(name = "category")
data class Category(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
        val name: String,
        @JoinColumn(referencedColumnName = "id") val parentCategory: Long? = null
)
