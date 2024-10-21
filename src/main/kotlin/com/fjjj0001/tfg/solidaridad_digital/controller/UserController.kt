package com.fjjj0001.tfg.solidaridad_digital.controller

import com.fjjj0001.tfg.solidaridad_digital.model.dto.MessageDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.UserDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.UserRatingDto
import com.fjjj0001.tfg.solidaridad_digital.security.JWTUtil
import com.fjjj0001.tfg.solidaridad_digital.service.HelpPublicationService
import com.fjjj0001.tfg.solidaridad_digital.service.UserService
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserAlreadyExistsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.io.Serializable
import java.security.Principal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping("/user")
class UserController {
    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtUtil: JWTUtil

    @Autowired
    private lateinit var encoder: PasswordEncoder

    @Autowired
    private lateinit var helpPublicationService: HelpPublicationService

    @PostMapping("/")
    fun register(@RequestBody userDto: UserDto): ResponseEntity<Map<String, Serializable>> {
        return try {
            userDto.aidsCompleted = mutableListOf()
            val createdUser = userService.createNewUser(
                userDto.dni!!,
                userDto.name!!,
                userDto.surname!!,
                userDto.username!!,
                userDto.email!!,
                userDto.phone!!,
                userDto.birthdate!!,
                userDto.password!!
            )

            val token = jwtUtil.generateToken(createdUser.email)
            val cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7200)
                .build()

            val response = mapOf(
                "token" to token,
                "user" to UserDto.fromUser(createdUser)
            )

            return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(response)
        } catch (e: UserAlreadyExistsException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun getLoggedInUserData(principal: Principal): ResponseEntity<UserDto> {
        val user = userService.getUserByEmail(principal.name)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserDto.fromUser(user))
    }

    @PostMapping("/login")
    fun login(@RequestBody userDto: UserDto): ResponseEntity<Map<String, Serializable>> {
        val user = userService.getUserByEmail(userDto.email!!)
            ?: return ResponseEntity.notFound().build()
        if (encoder.matches(userDto.password, user.password)) {
            val token = jwtUtil.generateToken(user.email)
            val cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7200)
                .build()

            val response = mapOf(
                "token" to token,
                "user" to UserDto.fromUser(user)
            )

            return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(response)
        } else {
            return ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    fun logout(): ResponseEntity<Void> {
        val cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .build()

        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .build()
    }

    @PutMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun updateUser(@RequestBody userDto: UserDto, principal: Principal): ResponseEntity<Map<String, Serializable>> {
        val user = userService.getUserByDni(userDto.dni!!)
            ?: return ResponseEntity.notFound().build()

        if (user.email != principal.name) return ResponseEntity.badRequest().build()

        if (userDto.newPassword != null) {
            if (!encoder.matches(userDto.password, user.password)) return ResponseEntity.badRequest().build()
            else user.password = encoder.encode(userDto.newPassword)
        } else {
            if (userService.getUserByEmail(userDto.email!!) != null && userDto.email != user.email) return ResponseEntity.badRequest()
                .build()

            user.name = userDto.name!!
            user.surname = userDto.surname!!
            user.email = userDto.email
            user.phone = userDto.phone!!
            user.birthdate = userDto.birthdate!!
        }

        val updatedUser = userService.updateUser(user)

        val token = jwtUtil.generateToken(updatedUser.email)
        val cookie = ResponseCookie.from("token", token)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(7200)
            .build()

        val response = mapOf(
            "token" to token,
            "user" to UserDto.fromUser(user)
        )

        return ResponseEntity.ok()
            .header("Set-Cookie", cookie.toString())
            .body(response)
    }

    @DeleteMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun deleteUser(principal: Principal): ResponseEntity<Void> {
        val user = userService.getUserByEmail(principal.name)
            ?: return ResponseEntity.notFound().build()

        val helpPublications = helpPublicationService.getHelpPublicationsByUser(user)
        for (h in helpPublications) {
            helpPublicationService.deleteHelpPublication(h.id!!)
        }

        userService.deleteUser(principal.name)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{username}")
    @PreAuthorize("isAuthenticated()")
    fun getUserByUsername(@PathVariable username: String): ResponseEntity<UserDto> {
        val user = userService.getUserByUsername(username)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserDto.fromUser(user))
    }

    //Ranking
    @GetMapping("/ranking")
    @PreAuthorize("isAuthenticated()")
    fun getTop10UsersByAidsCompleted(@RequestParam("dateString") dateString: String): ResponseEntity<List<UserDto>> {
        return try {
            val date = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
            val users = userService.getTop10UsersByAidsCompleted(date)
            return ResponseEntity.ok(users.map { UserDto.fromUser(it) })
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    //Rating
    @PostMapping("/rating")
    @PreAuthorize("isAuthenticated()")
    fun rateUser(@RequestBody ratingDto: UserRatingDto, principal: Principal): ResponseEntity<UserDto> {
        try {
            userService.addRating(
                principal.name,
                ratingDto.ratingUserUsername!!,
                ratingDto.rating!!,
                ratingDto.message!!
            )
            return ResponseEntity.ok(
                userService.getUserByUsername(ratingDto.ratingUserUsername!!)?.let { UserDto.fromUser(it) })
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    @DeleteMapping("/rating/{targetUserUsername}/{ratingId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteRating(
        @PathVariable("targetUserUsername") targetUserUsername: String,
        @PathVariable("ratingId") ratingId: Long,
        principal: Principal
    ): ResponseEntity<Void> {
        try {
            userService.deleteUserRating(
                ratingId,
                principal.name,
                targetUserUsername
            )

            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    //Messages
    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    fun sendMessage(@RequestBody messageDto: MessageDto, principal: Principal): ResponseEntity<Void> {
        try {
            userService.sendMessage(
                principal.name,
                messageDto.receiverUsername!!,
                messageDto.message!!
            )
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    fun getChatList(principal: Principal): ResponseEntity<List<String>> {
        try {
            val chatList = userService.getChatList(principal.name)
            return ResponseEntity.ok(chatList)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/messages/{otherUsername}")
    @PreAuthorize("isAuthenticated()")
    fun getChat(
        @PathVariable("otherUsername") otherUsername: String,
        principal: Principal
    ): ResponseEntity<List<MessageDto>> {
        try {
            val chat = userService.getChat(principal.name, otherUsername)
            return ResponseEntity.ok(chat.map { MessageDto.fromMessage(it) })
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }
}