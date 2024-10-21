package com.fjjj0001.tfg.solidaridad_digital.repository;

import com.fjjj0001.tfg.solidaridad_digital.model.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MessageRepository : JpaRepository<Message, Long> {


    @Query("SELECT rm FROM User u JOIN u.receivedMessages rm WHERE u.username = :username AND rm.sender.username = :username2")
    fun getReceivedMessagesByUsername(@Param("username") username: String, @Param("username2") username2: String): List<Message>
}