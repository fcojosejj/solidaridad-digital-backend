package com.fjjj0001.tfg.solidaridad_digital.model.dto

import com.fjjj0001.tfg.solidaridad_digital.model.Message
import java.io.Serializable
import java.time.LocalDateTime

data class MessageDto(
    var senderUsername: String? = null,
    var receiverUsername: String? = null,
    var message: String? = null,
    var date: LocalDateTime? = null,
    var id: Long? = null
) : Serializable {
    companion object {
        fun fromMessage(message: Message): MessageDto {
            return MessageDto(
                senderUsername = message.sender.username,
                message = message.message,
                date = message.date,
                id = message.id
            )
        }
    }
}