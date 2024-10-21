package com.fjjj0001.tfg.solidaridad_digital.service

import com.fjjj0001.tfg.solidaridad_digital.model.User
import com.fjjj0001.tfg.solidaridad_digital.util.exceptions.CommentNotFoundException
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
class HelpPublicationServiceTest {

    private lateinit var user: User
    private lateinit var media: ByteArray

    @Autowired
    private lateinit var helpPublicationService: HelpPublicationService

    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun init(){
        user = User(
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
        userService.createNewUser(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@"
        )

        val smallString: String = "."
        media = smallString.toByteArray()
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun createNewHelpPublicationTest() {
        assertDoesNotThrow {
            helpPublicationService.createHelpPublication(
                user,
                "Test Publication",
                "Test publication created for testing purposes",
                mutableListOf(media),
                mutableListOf("test", "publication", "tags"),
                LocalDateTime.now()
            )
        }

        assertThrows<ConstraintViolationException> {
            helpPublicationService.createHelpPublication(
                user,
                "T",
                "T",
                mutableListOf(media),
                mutableListOf(),
                LocalDateTime.now()
            )
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationByIdTest() {
        Assertions.assertNull(helpPublicationService.getHelpPublicationById(1))

        helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )
        Assertions.assertNotNull(helpPublicationService.getHelpPublicationById(1))
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationByTagsTest() {
        var result = helpPublicationService.getHelpPublicationByTags(listOf("test", "publication", "tags"))
        Assertions.assertEquals(0, result.size)

        helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )
        result = helpPublicationService.getHelpPublicationByTags(listOf("test"))
        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)

        result = helpPublicationService.getHelpPublicationByTags(listOf())
        Assertions.assertEquals(0, result.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationByPublicationDateTest() {
        //It returns an empty list
        Assertions.assertNotNull(helpPublicationService.getHelpPublicationByPublicationDate(LocalDate.now(), LocalDate.now()))

        helpPublicationService.createHelpPublication(user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )
        Assertions.assertNotNull(helpPublicationService.getHelpPublicationByPublicationDate(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)))
        Assertions.assertNotNull(helpPublicationService.getHelpPublicationByPublicationDate(LocalDate.now(), LocalDate.now()))
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationByUserTest() {
        var result = helpPublicationService.getHelpPublicationsByUser(user)
        Assertions.assertEquals(0, result.size)

        val resultHelpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )
        result = helpPublicationService.getHelpPublicationsByUser(resultHelpPublication.user)
        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateHelpPublicationTest() {
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        helpPublication.title = "Updated title"
        helpPublication.description = "Updated description"
        helpPublication.tags = mutableListOf("updated", "tags")
        helpPublication.publicationDate = LocalDateTime.now()

        val updatedHelpPublication = helpPublicationService.updateHelpPublication(helpPublication)
        Assertions.assertEquals("Updated title", updatedHelpPublication.title)
        Assertions.assertEquals("Updated description", updatedHelpPublication.description)
        Assertions.assertEquals("updated", updatedHelpPublication.tags[0])
        Assertions.assertEquals("tags", updatedHelpPublication.tags[1])
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteHelpPublicationTest() {
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        helpPublicationService.deleteHelpPublication(helpPublication.id!!)
        Assertions.assertNull(helpPublicationService.getHelpPublicationById(helpPublication.id!!))
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun findHelpPublicationByTitleTest() {
        var result = helpPublicationService.findHelpPublicationByTitle("Test Publication")
        Assertions.assertEquals(0, result.size)

        helpPublicationService.createHelpPublication(
            user,
            "Test Publication for testing purposes",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        helpPublicationService.createHelpPublication(
            user,
            "A different one for testing purposes",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        // Testing the normalization of the title
        result = helpPublicationService.findHelpPublicationByTitle("TeSTing")
        Assertions.assertNotNull(result)
        Assertions.assertEquals(2, result.size)

        // Testing a substring of the title
        result = helpPublicationService.findHelpPublicationByTitle("diff")
        Assertions.assertNotNull(result)
        Assertions.assertEquals(1, result.size)
    }

    // Comments tests
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun createCommentTest() {
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        val helpPublicationWithComment = helpPublicationService.createComment(user.email, 1, "Test comment")
        Assertions.assertEquals(1, helpPublicationWithComment.comments.size)
        Assertions.assertEquals("Test comment", helpPublicationWithComment.comments[0].text)

        // Only one comment is created
        Assertions.assertEquals(1, helpPublicationWithComment.comments[0].id)
        Assertions.assertEquals(user.dni, helpPublicationWithComment.comments[0].user.dni)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getCommentsByHelpPublicationTest(){
        var helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        helpPublicationService.createComment(user.email, 1, "Test comment")
        val helpPublicationWithComments = helpPublicationService.createComment(user.email, 1, "Test comment 2")

        Assertions.assertEquals(2, helpPublicationWithComments.comments.size)
        Assertions.assertEquals("Test comment", helpPublicationWithComments.comments[0].text)
        Assertions.assertEquals(1, helpPublicationWithComments.comments[0].id)
        Assertions.assertEquals("Test comment 2", helpPublicationWithComments.comments[1].text)
        Assertions.assertEquals(2, helpPublicationWithComments.comments[1].id)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateCommentTest(){
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        val helpPublicationWithComment = helpPublicationService.createComment(user.email, 1, "Test comment")
        val updatedHelpPublication = helpPublicationService.updateComment(helpPublicationWithComment, 1, "Updated comment")
        Assertions.assertEquals(1, updatedHelpPublication.comments.size)
        Assertions.assertEquals("Updated comment", updatedHelpPublication.comments[0].text)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteCommentTest(){
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication",
            "Test publication created for testing purposes",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now()
        )

        helpPublicationService.createComment(user.email, 1, "Test comment")
        var helpPublicationWithComment = helpPublicationService.createComment(user.email, 1, "Test comment 2")

        Assertions.assertEquals(2, helpPublicationWithComment.comments.size)
        helpPublicationWithComment = helpPublicationService.deleteComment(helpPublicationWithComment.id!!, 1)
        Assertions.assertEquals(1, helpPublicationWithComment.comments.size)
        Assertions.assertEquals("Test comment 2", helpPublicationWithComment.comments[0].text)

        assertThrows<CommentNotFoundException> {
            helpPublicationService.deleteComment(helpPublicationWithComment.id!!, 12345)
        }
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationsBySearchFilterTest(){
        val helpPublication = helpPublicationService.createHelpPublication(
            user,
            "Test Publication 1",
            "Test publication created for testing purposes 1",
            mutableListOf(media),
            mutableListOf("test", "publication", "tags"),
            LocalDateTime.now().minusDays(2)
        )

        val helpPublication2 = helpPublicationService.createHelpPublication(
            user,
            "Test Publication 2",
            "Test publication created for testing purposes 2",
            mutableListOf(media),
            mutableListOf("test", "help", "array"),
            LocalDateTime.now()
        )

        val helpPublication3 = helpPublicationService.createHelpPublication(
            user,
            "Test Publication 3",
            "Test publication created for testing purposes 3",
            mutableListOf(media),
            mutableListOf("last", "publication", "created"),
            LocalDateTime.now().plusDays(2)
        )

        val resultTitle = helpPublicationService.getHelpPublicationsBySearchFilter(null, "Test Publication", null, null, null)
        Assertions.assertEquals(3, resultTitle.size)

        val resultTitleAndTags = helpPublicationService.getHelpPublicationsBySearchFilter(null, "Test Publication", mutableListOf("publication"), null, null)
        Assertions.assertEquals(2, resultTitleAndTags.size)

        val resultUsername = helpPublicationService.getHelpPublicationsBySearchFilter(user.username, null, null, null, null)
        Assertions.assertEquals(3, resultUsername.size)

        val resultInitDate = helpPublicationService.getHelpPublicationsBySearchFilter(null, null, null,
            LocalDate.now().minusDays(3).toString(), null)
        Assertions.assertEquals(3, resultInitDate.size)

        val resultFinalDate = helpPublicationService.getHelpPublicationsBySearchFilter(null, null, null, null,
            LocalDate.now().minusDays(3).toString()
        )
        Assertions.assertEquals(0, resultFinalDate.size)

        val resultBothDates = helpPublicationService.getHelpPublicationsBySearchFilter(null, null, null,
            LocalDate.now().minusDays(4).toString(), LocalDate.now().toString()
        )
        Assertions.assertEquals(2, resultBothDates.size)

        val resultAll = helpPublicationService.getHelpPublicationsBySearchFilter(user.username, "Test Publication", mutableListOf("test"),
            LocalDate.now().plusDays(1).toString(), LocalDate.now().plusDays(5).toString()
        )
        Assertions.assertEquals(0, resultAll.size)
    }
}