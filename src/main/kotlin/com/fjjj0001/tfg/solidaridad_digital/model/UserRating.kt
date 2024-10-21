package com.fjjj0001.tfg.solidaridad_digital.model

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size

@Entity
class UserRating(
    @ManyToOne
    val ratedBy: User,

    @field:DecimalMin(value = "1", inclusive = true)
    @field:DecimalMax(value = "5", inclusive = true)
    val rating: Int,

    @field:Size(min = 0, max = 128)
    val message: String,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)