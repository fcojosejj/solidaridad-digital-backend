package com.fjjj0001.tfg.solidaridad_digital.model.dto

import com.fjjj0001.tfg.solidaridad_digital.model.HelpPublication
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*


data class HelpPublicationDto(
    val userUsername: String? = null,
    var title: String? = null,
    val description: String? = null,
    val media: List<String>? = null,
    val tags: MutableList<String>? = null,
    val publicationDate: LocalDateTime? = null,
    val editDate: LocalDateTime? = null,
    val initialDate: String? = null,
    val finalDate: String? = null,
    val comments: MutableList<CommentDto>? = null,
    var helperUsername: String? = null,
    val id: Long? = null
) : Serializable {
    companion object {
        fun fromHelpPublication(helpPublication: HelpPublication): HelpPublicationDto {
            return HelpPublicationDto(
                id = helpPublication.id,
                title = helpPublication.title,
                description = helpPublication.description,
                media = helpPublication.media.map { Base64.getEncoder().encodeToString(it) },
                userUsername = helpPublication.user.username,
                tags = helpPublication.tags,
                publicationDate = helpPublication.publicationDate,
                editDate = helpPublication.editDate,
                comments = helpPublication.comments.map { CommentDto.fromComment(it) }.toMutableList(),
                helperUsername = helpPublication.helperUsername
            )
        }
    }
}