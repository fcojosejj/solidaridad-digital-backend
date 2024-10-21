package com.fjjj0001.tfg.solidaridad_digital.service

import com.fjjj0001.tfg.solidaridad_digital.model.User
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.InvalidPasswordException
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.UserNotRegisteredException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class UserServiceTests {

    private lateinit var invalidUser: User
    private lateinit var validUser: User
    @Autowired
    private lateinit var userService: UserService
    @Autowired
    private lateinit var helpPublicationService: HelpPublicationService

    @BeforeEach
    fun init(){
        invalidUser = User(
            "123B",
            "J",
            "D",
            "e",
            "no_email",
            "123",
            LocalDate.now(),
            "password",
            mutableListOf()
        )

        validUser = User(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            mutableListOf()
        )
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun createNewUserTest() {
        assertDoesNotThrow {
            userService.createNewUser(
                validUser.dni,
                validUser.name,
                validUser.surname,
                validUser.username,
                validUser.email,
                validUser.phone,
                validUser.birthdate,
                validUser.password
            )
        }

        assertThrows<InvalidPasswordException> {
            userService.createNewUser(
                invalidUser.dni,
                invalidUser.name,
                invalidUser.surname,
                invalidUser.username,
                invalidUser.email,
                invalidUser.phone,
                invalidUser.birthdate,
                invalidUser.password
            )
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getUserByEmailTest() {
        userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password)
        Assertions.assertEquals(validUser.email, userService.getUserByEmail(validUser.email)!!.email)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getUserByDniTest() {
        userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password)
        Assertions.assertEquals(validUser.dni, userService.getUserByDni(validUser.dni)!!.dni)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getUserByUsernameTest() {
        userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password)
        Assertions.assertEquals(validUser.username, userService.getUserByUsername(validUser.username)!!.username)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateUserTest() {
        userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )
        validUser.name = "Francisco"
        userService.updateUser(validUser)
        Assertions.assertEquals("Francisco", userService.getUserByDni(validUser.dni)!!.name)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteUserTest() {
        userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )
        userService.deleteUser(
            validUser.email
        )

        Assertions.assertNull(userService.getUserByDni(validUser.dni))
        Assertions.assertEquals(0, helpPublicationService.getHelpPublicationsByUser(validUser).size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getTop10UsersByAidsCompletedTest(){
        val user = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        user.aidsCompleted.add(LocalDateTime.now())
        user.aidsCompleted.add(LocalDateTime.now())
        user.aidsCompleted.add(LocalDateTime.now())

        userService.updateUser(user)
        val top10 = userService.getTop10UsersByAidsCompleted(LocalDateTime.now().minusDays(1))
        Assertions.assertEquals(1, top10.size)
    }

    @Test
    @Transactional
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun addRatingTest(){
        val user = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        assertThrows<UserNotRegisteredException> {
            userService.addRating(user.dni, "12456789B", 5, "Great user")
        }

        val targetUser = userService.createNewUser(
            "87654321B",
            "Juan",
            "Pérez",
            "juanp",
            "juanp@mail.com",
            "612345789",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        userService.addRating(user.email, targetUser.username, 5, "Great user")
        Assertions.assertEquals(1, userService.getUserByDni("87654321B")?.ratings?.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Transactional
    fun deleteUserRatingTest(){
        val user = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        assertThrows<UserNotRegisteredException> {
            userService.addRating(user.email, "pedrog", 5, "Great user")
        }

        val targetUser = userService.createNewUser(
            "87654321B",
            "Juan",
            "Pérez",
            "juanp",
            "juanp@mail.com",
            "612345789",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        val anotherUser = userService.createNewUser(
            "12345678B",
            "Pedro",
            "Gómez",
            "pedrog",
            "pedrog@mail.com",
            "612345449",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        userService.addRating(user.email, targetUser.username, 5, "Great user")
        userService.addRating(anotherUser.email, targetUser.username, 3, "Not so great user")
        Assertions.assertEquals(2, userService.getUserByDni("87654321B")?.ratings?.size)

        userService.deleteUserRating(1, user.email, targetUser.username)
        Assertions.assertEquals(1, userService.getUserByDni("87654321B")?.ratings?.size)

        userService.deleteUserRating(2, user.email, targetUser.username)
        Assertions.assertEquals(0, userService.getUserByDni("87654321B")?.ratings?.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Transactional
    fun receiveMessageTest(){
        val receiver = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        val sender = userService.createNewUser(
            "87654321B",
            "Juan",
            "Pérez",
            "juanp",
            "juanp@mail.com",
            "612345789",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        receiver.receiveMessage(sender, "Hello")
        Assertions.assertEquals(1, receiver.receivedMessages.size)
        Assertions.assertEquals("Hello", receiver.receivedMessages[0].message)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Transactional
    fun getChatListTest(){
        val user = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        val otherUser = userService.createNewUser(
            "87654321B",
            "Juan",
            "Pérez",
            "juanp",
            "juanp@mail.com",
            "612345789",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        val anotherUser = userService.createNewUser(
            "12345678B",
            "Pedro",
            "Gómez",
            "pedrog",
            "pedrog@mail.com",
            "612345449",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )

        user.receiveMessage(otherUser, "Hello")

        anotherUser.receiveMessage(user, "Hi")

        val chatList = userService.getChatList(user.email)
        val chatList2 = userService.getChatList(anotherUser.email)
        val chatList3 = userService.getChatList(otherUser.email)
        Assertions.assertEquals(2, chatList.size)
        Assertions.assertEquals(1, chatList2.size)
        Assertions.assertEquals(1, chatList3.size)

        Assertions.assertNotNull(chatList.find { it == otherUser.username })
        Assertions.assertNotNull(chatList.find { it == anotherUser.username })

        Assertions.assertNotNull(chatList2.find { it == user.username })
        Assertions.assertNotNull(chatList3.find { it == user.username })
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Transactional
    fun getChatTest(){
        val user = userService.createNewUser(
            validUser.dni,
            validUser.name,
            validUser.surname,
            validUser.username,
            validUser.email,
            validUser.phone,
            validUser.birthdate,
            validUser.password
        )

        val otherUser = userService.createNewUser(
            "87654321B",
            "Juan",
            "Pérez",
            "juanp",
            "juanp@mail.com",
            "612345789",
            LocalDate.of(1990, 1, 1),
            "Password2@"
        )


        user.receiveMessage(otherUser, "Hello")
        otherUser.receiveMessage(user, "Hi")
        user.receiveMessage(otherUser, "Hello again")

       val chat = userService.getChat(user.email, otherUser.username)
        Assertions.assertEquals(3, chat.size)
    }
}