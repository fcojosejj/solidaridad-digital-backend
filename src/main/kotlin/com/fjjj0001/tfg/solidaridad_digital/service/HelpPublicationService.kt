package com.fjjj0001.tfg.solidaridad_digital.service

import com.fjjj0001.tfg.solidaridad_digital.model.HelpPublication
import com.fjjj0001.tfg.solidaridad_digital.model.User
import com.fjjj0001.tfg.solidaridad_digital.repository.HelpPublicationRepository
import com.fjjj0001.tfg.solidaridad_digital.repository.UserRepository
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.CommentNotFoundException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.HelpPublicationNotFoundException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserCantAidHimselfException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserNotRegisteredException
import com.fjjj0001.tfg.solidaridad_digital.util.normalize
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Validated
class HelpPublicationService {
    @Autowired
    private lateinit var helpPublicationRepository: HelpPublicationRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    fun createHelpPublication(
        @Valid u: User,
        title: String,
        description: String,
        media: MutableList<ByteArray>?,
        tags: MutableList<String>,
        publicationDate: LocalDateTime
        ): HelpPublication{

        tags.forEach { it.normalize() }

        val newHelpPublication = HelpPublication (
            title = title,
            description = description,
            media = media!!,
            user = u,
            tags = tags,
            publicationDate = publicationDate
        )
        return helpPublicationRepository.save(newHelpPublication)
    }

    fun getHelpPublicationById(id: Long): HelpPublication? {
        return helpPublicationRepository.findById(id).orElse(null)
    }

    fun getHelpPublicationsByUser(@Valid u: User): List<HelpPublication> {
        return helpPublicationRepository.findByUser(u)
    }

    fun getHelpPublicationByTags(tags: List<String>): List<HelpPublication> {
        return helpPublicationRepository.findByTags(tags)
    }

    fun getHelpPublicationByPublicationDate(startDate: LocalDate, endDate: LocalDate): List<HelpPublication> {
        return helpPublicationRepository.findByPublicationDate(startDate, endDate)
    }

    fun findHelpPublicationByTitle(title: String): List<HelpPublication> {
        val normalizedTitle = title.normalize()
        return helpPublicationRepository.findAll().filter { it.title.normalize().contains(normalizedTitle) }
    }

    fun updateHelpPublication(@Valid helpPublication: HelpPublication): HelpPublication {
        return helpPublicationRepository.save(helpPublication)

    }

    fun deleteHelpPublication(id: Long) {
        helpPublicationRepository.deleteById(id)
    }

    fun getHelpPublicationsBySearchFilter(
        userUsername: String?,
        title: String?,
        tags: MutableList<String>?,
        initialDate: String?,
        finalDate: String?
    ): List<HelpPublication> {
        return helpPublicationRepository.findBySearchFilter(
            userUsername = userUsername,
            title = title,
            tags = tags,
            initialDate = initialDate?.let { LocalDate.parse(it).atStartOfDay() },
            finalDate = finalDate?.let { LocalDate.parse(it).atTime(23,59,59) }
        ).sortedByDescending { it.publicationDate }
    }

    fun confirmAid(
        userTargetUsername: String,
        loggedUserEmail: String,
        helpPublicationId: Long
    ) : HelpPublication {
        val userTarget = userRepository.findByUsername(userTargetUsername) ?: throw UserNotRegisteredException()
        val loggedUser = userRepository.findByEmail(loggedUserEmail) ?: throw UserNotRegisteredException()
        val helpPublication = helpPublicationRepository.findById(helpPublicationId).orElseThrow { HelpPublicationNotFoundException() }

        if (helpPublication.user.username == userTarget.username || loggedUser.username == userTarget.username) throw UserCantAidHimselfException()

        userTarget.aidsCompleted.add(LocalDateTime.now())
        helpPublication.helperUsername = userTarget.username
        userRepository.save(userTarget)
        return helpPublicationRepository.save(helpPublication)
    }

    // Comments
    fun createComment(
        userEmail : String,
        helpPublicationId: Long,
        text: String,
    ): HelpPublication {
        val user = userRepository.findByEmail(userEmail) ?: throw UserNotRegisteredException()
        val helpPublication = helpPublicationRepository.findById(helpPublicationId).orElseThrow { HelpPublicationNotFoundException() }

        helpPublication.addComment(
            user = user,
            comment = text
        )
        return helpPublicationRepository.save(helpPublication)
    }

    fun updateComment(
        @Valid helpPublication: HelpPublication,
        commentId: Long,
        newText: String
    ): HelpPublication {
        val comment = helpPublication.comments.find { it.id == commentId } ?: throw CommentNotFoundException()
        comment.text = newText
        comment.editDateTime = LocalDateTime.now()
        return helpPublicationRepository.save(helpPublication)
    }

    fun deleteComment(
        helpPublicationId: Long,
        commentId: Long
    ): HelpPublication {
        val helpPublication = helpPublicationRepository.findById(helpPublicationId).orElseThrow { HelpPublicationNotFoundException() }

        helpPublication.comments.removeIf { it.id == commentId }.also {
            if (!it) throw CommentNotFoundException()
        }
        return helpPublicationRepository.save(helpPublication)
    }
}