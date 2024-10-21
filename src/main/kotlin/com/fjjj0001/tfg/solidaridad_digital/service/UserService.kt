package com.fjjj0001.tfg.solidaridad_digital.service

import com.fjjj0001.tfg.solidaridad_digital.model.Message
import com.fjjj0001.tfg.solidaridad_digital.model.User
import com.fjjj0001.tfg.solidaridad_digital.repository.MessageRepository
import com.fjjj0001.tfg.solidaridad_digital.repository.UserRepository
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.InvalidPasswordException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserAlreadyExistsException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserNotRegisteredException
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Validated
class UserService {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @Autowired
    private lateinit var encoder: PasswordEncoder

    fun createNewUser(
        dni: String,
        name: String,
        surname: String,
        username: String,
        email: String,
        phone: String,
        birthdate: LocalDate,
        password: String
    ): User{
        if (userRepository.findByDni(dni) != null || userRepository.findByEmail(email) != null || userRepository.findByUsername(username) != null || userRepository.findByPhone(phone) != null){
            throw UserAlreadyExistsException()
        }

        val regex = Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
        if (!regex.matches(password)){
            throw InvalidPasswordException()
        }

        val u = User(dni, name, surname, username, email, phone, birthdate, password)
        u.password = encoder.encode(u.password)

        return userRepository.save(u)
    }

    fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun getUserByDni(dni: String): User? {
        return userRepository.findByDni(dni)
    }

    @Transactional
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun updateUser(@Valid user: User): User{
        return userRepository.save(user)
    }

    fun deleteUser(userEmail: String){
        val user = userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()
        userRepository.delete(user)
    }

    fun getTop10UsersByAidsCompleted(date: LocalDateTime): List<User>{
        val pageable = PageRequest.of(0, 10)
        return userRepository.findByAidsCompletedAfterDate(date, pageable)
    }

    @Transactional
    fun addRating(
        userEmail: String,
        targetUserUsername: String,
        rating: Int,
        message: String
    ) {
        val user = userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()
        val targetUser = userRepository.findByUsername(targetUserUsername) ?: throw UserNotRegisteredException()

        targetUser.addRating(user, rating, message)

        userRepository.save(targetUser)
    }

    @Transactional
    fun deleteUserRating(
        ratingId: Long,
        userEmail: String,
        targetUserUsername: String
    ) {
        userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()
        val targetUser = userRepository.findByUsername(targetUserUsername) ?: throw UserNotRegisteredException()
        targetUser.ratings.removeIf { it.id == ratingId }
        userRepository.save(targetUser)
    }

    @Transactional
    fun sendMessage(
        senderEmail: String,
        receiverUsername: String,
        message: String
    ){
        val receiver = userRepository.findByUsername(receiverUsername) ?: throw UserNotRegisteredException()
        val sender = userRepository.findByEmail(senderEmail) ?: throw UserNotRegisteredException()

        receiver.receiveMessage(sender, message)

        userRepository.save(receiver)
    }

    @Transactional
    fun getChat(
        userEmail: String,
        otherUsername: String
    ): List<Message> {
        val user = userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()
        userRepository.findByUsername(otherUsername) ?: throw UserNotRegisteredException()

        val userMessages = messageRepository.getReceivedMessagesByUsername(otherUsername, user.username)
        val otherUserMessages = messageRepository.getReceivedMessagesByUsername(user.username, otherUsername)

        return (userMessages + otherUserMessages).sortedBy { it.date }
    }

    @Transactional
    fun getChatList(
        userEmail: String
    ): List<String> {
        val user = userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()

        val usersWhoSentMessageToUser = userRepository.findUsersWhoSentMessagesToUsername(user.username)
        val usersWhoReceivedMessageFromUser = userRepository.findUsersWhoReceivedMessageFromLoggedUser(user.username)

        return (usersWhoSentMessageToUser + usersWhoReceivedMessageFromUser).distinct()
    }


}