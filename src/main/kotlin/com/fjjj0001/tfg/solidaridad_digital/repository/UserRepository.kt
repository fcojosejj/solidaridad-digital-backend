package com.fjjj0001.tfg.solidaridad_digital.repository;

import com.fjjj0001.tfg.solidaridad_digital.model.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface UserRepository : JpaRepository<User, String> {
    fun findByEmail(email: String): User?
    fun findByDni(dni: String): User?
    fun findByUsername(username: String): User?
    fun findByPhone(phone: String): User?

    @Query("SELECT u FROM User u JOIN u.aidsCompleted a WHERE a > :date GROUP BY u ORDER BY COUNT(a) DESC")
    fun findByAidsCompletedAfterDate(@Param("date") date: LocalDateTime, pageable: Pageable): List<User>

    @Query(" SELECT DISTINCT m.sender.username FROM User u JOIN u.receivedMessages m WHERE u.username = :username AND m.sender.username != :username")
    fun findUsersWhoSentMessagesToUsername(@Param("username") username: String): List<String>

    @Query("SELECT DISTINCT u.username FROM User u LEFT JOIN u.receivedMessages rm WHERE rm.sender.username = :username")
    fun findUsersWhoReceivedMessageFromLoggedUser(@Param("username") username: String): List<String>
}