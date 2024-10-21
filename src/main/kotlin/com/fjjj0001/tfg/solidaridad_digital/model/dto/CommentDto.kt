package com.fjjj0001.tfg.solidaridad_digital.model.dto

import com.fjjj0001.tfg.solidaridad_digital.model.Comment
import java.io.Serializable
import java.time.LocalDateTime

data class CommentDto(
    var userUsername: String? = null,
    var publicationDate: LocalDateTime? = null,
    var editDateTime: LocalDateTime? = null,
    var text: String? = null,
    var helpPublicationId: Long? = null,
    var id: Long? = null
) : Serializable {
    companion object {
        fun fromComment(comment: Comment): CommentDto {
            return CommentDto(
                id = comment.id,
                userUsername = comment.user.username,
                publicationDate = comment.publicationDate,
                editDateTime = comment.editDateTime,
                text = comment.text
            )
        }
    }
}