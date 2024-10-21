package com.fjjj0001.tfg.solidaridad_digital.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
class HelpPublication(
    @ManyToOne
    val user: User,

    @field:NotBlank
    @field:Size(min = 2, max = 64)
    var title: String,

    @field:NotBlank
    @field:Size(min = 16, max = 1024)
    var description: String,

    @ElementCollection(fetch = FetchType.EAGER)
    var media: MutableList<ByteArray> = mutableListOf(),

    @ElementCollection(fetch = FetchType.EAGER)
    var tags: MutableList<String> = mutableListOf(),

    @field:NotNull
    var publicationDate: LocalDateTime,

    var editDate: LocalDateTime? = null,

    var helperUsername: String? = null,

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    val comments: MutableList<Comment> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {

    fun addComment(
        user: User,
        comment: String,
    ) {
        if (comment.length < 4 || comment.length > 512) throw IllegalArgumentException("Comment must be between 4 and 512 characters")
        comments.add(
            Comment(
                user = user,
                publicationDate = LocalDateTime.now(),
                text = comment
            )
        )
    }
}