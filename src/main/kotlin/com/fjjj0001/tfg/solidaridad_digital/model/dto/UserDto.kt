package com.fjjj0001.tfg.solidaridad_digital.model.dto

import com.fjjj0001.tfg.solidaridad_digital.model.User
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime


data class UserDto(
    val dni: String? = null,
    val name: String? = null,
    val surname: String? = null,
    val username: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val birthdate: LocalDate? = null,
    val password: String? = null,
    val newPassword: String? = null,
    var aidsCompleted: MutableList<LocalDateTime>? = null,
    var ratings: MutableList<UserRatingDto>? = null,
    var receivedMessages: MutableList<MessageDto>? = null
) : Serializable {
    fun toUser(): User {
        return User(
            dni = dni!!,
            name = name!!,
            surname = surname!!,
            username = username!!,
            email = email!!,
            phone = phone!!,
            birthdate = birthdate!!,
            password = password!!,
            aidsCompleted = aidsCompleted!!
        )
    }

    companion object {
        fun fromUser(user: User): UserDto {
            return UserDto(
                dni = user.dni,
                name = user.name,
                surname = user.surname,
                username = user.username,
                email = user.email,
                phone = user.phone,
                birthdate = user.birthdate,
                aidsCompleted = user.aidsCompleted,
                ratings = user.ratings.map { UserRatingDto.fromUserRating(it) }.toMutableList(),
                receivedMessages = user.receivedMessages.map { MessageDto.fromMessage(it) }.toMutableList()
            )
        }
    }
}