package com.fjjj0001.tfg.solidaridad_digital.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    val user: User,

    @field: NotNull
    val publicationDate: LocalDateTime,

    var editDateTime: LocalDateTime? = null,

    @field:NotBlank
    @field:Size(min = 4, max = 512)
    var text: String,
) {
}