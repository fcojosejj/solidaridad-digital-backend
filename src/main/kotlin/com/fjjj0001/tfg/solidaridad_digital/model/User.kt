package com.fjjj0001.tfg.solidaridad_digital.model

import com.fjjj0001.tfg.solidaridad_digital.util.OlderThan
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserAlreadyRatedException
import jakarta.persistence.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class User (
    @field:Pattern(regexp = "\\d{8}[A-HJ-NP-TV-Z]")
    @Id
    val dni: String,

    @field:NotBlank
    @field:Size(min = 2, max = 20)
    var name: String,

    @field:NotBlank
    @field:Size(min = 2, max = 64)
    var surname: String,

    @field:NotBlank
    @field:Size(min = 2, max = 10)
    var username: String,

    @field:Email
    var email: String,

    @field:NotBlank
    @field:Pattern(regexp = "(\\+34|0034|34)?[6789]\\d{8}\$")
    var phone: String,

    @field:OlderThan(
        age = 18
    )
    var birthdate: LocalDate,

    @field:NotBlank
    var password: String,

    @ElementCollection(fetch = FetchType.EAGER)
    var aidsCompleted: MutableList<LocalDateTime> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user_dni")
    var ratings: MutableList<UserRating> = mutableListOf(),

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "receiver_dni")
    var receivedMessages: MutableList<Message> = mutableListOf(),
) {
    fun addRating(
        ratedBy: User,
        rating: Int,
        message: String
    ){
        if (ratings.any { it.ratedBy == ratedBy }) throw UserAlreadyRatedException()
        if (rating !in 1..5) throw IllegalArgumentException("Rating must be between 1 and 5")
        if (message.length !in 0..128) throw IllegalArgumentException("Message must be less than 128 characters")

        ratings.add(UserRating(ratedBy, rating, message))
    }

    fun receiveMessage(
        sentBy: User,
        message: String
    ){
        if (message.length !in 1..512) throw IllegalArgumentException("Message must be between 1 and 512 characters long")

        receivedMessages.add(Message(sentBy, message, LocalDateTime.now()))
    }
}