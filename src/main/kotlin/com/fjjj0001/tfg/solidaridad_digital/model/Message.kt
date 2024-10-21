package com.fjjj0001.tfg.solidaridad_digital.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class Message (
    @ManyToOne
    val sender: User,

    @field:Size(min = 1, max = 512)
    val message: String,

    @field:NotNull
    val date: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)