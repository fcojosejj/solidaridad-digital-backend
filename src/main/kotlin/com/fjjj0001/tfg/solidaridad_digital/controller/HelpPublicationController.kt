package com.fjjj0001.tfg.solidaridad_digital.controller

import com.fjjj0001.tfg.solidaridad_digital.model.dto.CommentDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.HelpPublicationDto
import com.fjjj0001.tfg.solidaridad_digital.service.HelpPublicationService
import com.fjjj0001.tfg.solidaridad_digital.service.UserService
import com.fjjj0001.tfg.solidaridad_digital.util.normalize
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal
import java.time.LocalDateTime

@RestController
@RequestMapping("/helpPublications")
class HelpPublicationController {
    @Autowired
    private lateinit var helpPublicationService: HelpPublicationService

    @Autowired
    private lateinit var userService: UserService

    @PostMapping("/", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("isAuthenticated()")
    fun createHelpPublication(
        @RequestParam("title") title: String,
        @RequestParam("description") description: String,
        @RequestParam("media", required = false) media: List<MultipartFile>?,
        @RequestParam("tags") jsonTags: String,
        principal: Principal
    ): ResponseEntity<HelpPublicationDto> {
        return try {
            val mediaList = media?.map { it.bytes }?.toMutableList() ?: mutableListOf()

            val newHelpPublication = helpPublicationService.createHelpPublication(
                userService.getUserByEmail(principal.name)!!,
                title,
                description,
                mediaList,
                jsonTags.split(",").map{ it.normalize() }.toMutableList(),
                LocalDateTime.now()
            )

            ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(newHelpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun updateHelpPublication(@RequestBody helpPublicationDto: HelpPublicationDto, principal: Principal): ResponseEntity<HelpPublicationDto> {
        var helpPublication = helpPublicationService.getHelpPublicationById(helpPublicationDto.id!!)
            ?: return ResponseEntity.notFound().build()

        if (helpPublication.user.email != principal.name) return ResponseEntity.badRequest().build()

        helpPublication.title = helpPublicationDto.title!!
        helpPublication.description = helpPublicationDto.description!!
        helpPublication.tags = helpPublicationDto.tags!!.map { it.normalize() }.toMutableList()
        helpPublication.editDate = LocalDateTime.now()

        val updatedHelpPublication = helpPublicationService.updateHelpPublication(helpPublication)
        return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(updatedHelpPublication))
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getHelpPublicationById(@PathVariable("id") id: Long): ResponseEntity<HelpPublicationDto> {
        return try {
            val helpPublication = helpPublicationService.getHelpPublicationById(id)
                ?: return ResponseEntity.notFound().build()
            return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(helpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun deleteHelpPublicationById(@PathVariable("id") id: Long): ResponseEntity<Void> {
        return try {
            helpPublicationService.deleteHelpPublication(id)
            ResponseEntity.ok().build()
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    fun getHelpPublicationsBySearchFilter(
        @RequestParam username: String?,
        @RequestParam title: String?,
        @RequestParam tags: String?,
        @RequestParam initialDate: String?,
        @RequestParam finalDate: String?
        ) : ResponseEntity<List<HelpPublicationDto>> {
        return try {
            val helpPublications = helpPublicationService.getHelpPublicationsBySearchFilter(
                username.takeIf { it != "" },
                title.takeIf { it != "" },
                tags.takeIf { it != "" }?.split(",")?.map{ it.normalize() }?.toMutableList(),
                initialDate.takeIf { it != "" },
                finalDate.takeIf { it != "" })

            ResponseEntity.ok(helpPublications.map { HelpPublicationDto.fromHelpPublication(it) })
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/aid")
    @PreAuthorize("isAuthenticated()")
    fun confirmAid(
        @RequestParam username: String,
        @RequestParam helpPublicationId: Long,
        principal: Principal): ResponseEntity<HelpPublicationDto> {
        return try{
            val helpPublication = helpPublicationService.confirmAid(username, principal.name, helpPublicationId)

            return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(helpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    // Comments
    @PostMapping("/comment")
    @PreAuthorize("isAuthenticated()")
    fun createComment(
        @RequestBody commentDto: CommentDto,
        principal: Principal
    ): ResponseEntity<HelpPublicationDto> {
        return try {
            val updatedHelpPublication = helpPublicationService.createComment(principal.name, commentDto.helpPublicationId!!, commentDto.text!!)

            return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(updatedHelpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/comment")
    @PreAuthorize("isAuthenticated()")
    fun updateComment(
        @RequestBody commentDto: CommentDto,
        principal: Principal
    ): ResponseEntity<HelpPublicationDto> {
        return try {
            val helpPublication = helpPublicationService.getHelpPublicationById(commentDto.helpPublicationId!!)
                ?: return ResponseEntity.notFound().build()

            if (helpPublication.comments.find { it.id == commentDto.id }?.user?.email != principal.name) return ResponseEntity.badRequest().build()

            val updatedHelpPublication = helpPublicationService.updateComment(helpPublication, commentDto.id!!, commentDto.text!!)

            return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(updatedHelpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/comment")
    @PreAuthorize("isAuthenticated()")
    fun deleteComment(
        @RequestBody commentDto: CommentDto,
        principal: Principal
    ): ResponseEntity<HelpPublicationDto> {
        return try {
            val helpPublication = helpPublicationService.getHelpPublicationById(commentDto.helpPublicationId!!)
                ?: return ResponseEntity.notFound().build()

            if (helpPublication.comments.find { it.id == commentDto.id }?.user?.email != principal.name) return ResponseEntity.badRequest().build()

            val updatedHelpPublication = helpPublicationService.deleteComment(helpPublication.id!!, commentDto.id!!)

            return ResponseEntity.ok(HelpPublicationDto.fromHelpPublication(updatedHelpPublication))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}