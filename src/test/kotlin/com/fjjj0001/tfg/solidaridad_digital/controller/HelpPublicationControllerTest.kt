package com.fjjj0001.tfg.solidaridad_digital.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fjjj0001.tfg.solidaridad_digital.model.dto.CommentDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.HelpPublicationDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.HelpPublicationSearchFilterDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.UserDto
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HelpPublicationControllerTest {

    private lateinit var media: ByteArray

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jacksonConverter: MappingJackson2HttpMessageConverter
    private lateinit var restTemplate: TestRestTemplate
    private lateinit var objectMapper: ObjectMapper

    @PostConstruct
    fun init() {
        val smallString = "."
        media = smallString.toByteArray()
        objectMapper = jacksonConverter.objectMapper
        val restTemplateBuilder = RestTemplateBuilder()
            .rootUri("http://localhost:$port")
            .additionalMessageConverters(jacksonConverter, FormHttpMessageConverter())

        restTemplate = TestRestTemplate(restTemplateBuilder)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun createHelpPublicationTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        val jwtToken = responseUser.body?.get("token").toString()

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken)
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        val responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(responseHelpPublication.body?.title, helpPublicationDto.title)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteHelpPublicationTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        val responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        val headersDelete = HttpHeaders()
        headersDelete.setBearerAuth(jwtToken.toString())
        val responseHelpPublicationDto = responseHelpPublication.body as HelpPublicationDto
        val deleteEntity = HttpEntity(responseHelpPublicationDto, headersDelete)

        // Delete help publication
        val deleteHelpPublication = restTemplate.exchange("/helpPublications/${responseHelpPublication.body?.id}", HttpMethod.DELETE, deleteEntity, Void::class.java)
        Assertions.assertEquals(deleteHelpPublication.statusCode.is2xxSuccessful, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateHelpPublicationTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test Publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "Publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help Publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        var responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        responseHelpPublication.body?.title = "Updated Test Publication"

        val updateHeaders = HttpHeaders()
        updateHeaders.setBearerAuth(jwtToken.toString())
        val updateEntity = HttpEntity(responseHelpPublication.body, updateHeaders)
        val updateResponse = restTemplate.exchange("/helpPublications/", HttpMethod.PUT, updateEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(updateResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(updateResponse.body?.title, "Updated Test Publication")
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationByIdTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        var responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        val getHeaders = HttpHeaders()
        getHeaders.setBearerAuth(jwtToken.toString())
        val getEntity = HttpEntity(responseHelpPublication.body, getHeaders)
        val getResponse = restTemplate.exchange("/helpPublications/${responseHelpPublication.body?.id!!}", HttpMethod.GET, getEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(getResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(getResponse.body?.title, helpPublicationDto.title)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun createCommentTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        var responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        // Create a new comment
        val commentDto = CommentDto(
            helpPublicationId = responseHelpPublication.body?.id,
            text = "This is a test comment",
        )

        val commentHeaders = HttpHeaders()
        commentHeaders.setBearerAuth(jwtToken.toString())
        val commentEntity = HttpEntity(commentDto, commentHeaders)

        val commentResponse = restTemplate.exchange("/helpPublications/comment", HttpMethod.POST, commentEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(commentResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(commentResponse.body?.comments?.size, 1)
        Assertions.assertEquals(commentResponse.body?.comments?.get(0)?.text, commentDto.text)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateCommentTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        var responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        // Create a new comment
        val commentDto = CommentDto(
            helpPublicationId = responseHelpPublication.body?.id,
            text = "This is a test comment",
        )

        val commentHeaders = HttpHeaders()
        commentHeaders.setBearerAuth(jwtToken.toString())
        val commentEntity = HttpEntity(commentDto, commentHeaders)

        val commentResponse =
            restTemplate.exchange("/helpPublications/comment", HttpMethod.POST, commentEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(commentResponse.statusCode.is2xxSuccessful, true)

        val updatedCommentDto = commentResponse.body?.comments?.get(0)
        updatedCommentDto?.text = "This is an updated test comment"
        updatedCommentDto?.helpPublicationId = responseHelpPublication.body?.id
        val updateEntity = HttpEntity(updatedCommentDto, commentHeaders)

        val updateResponse = restTemplate.exchange("/helpPublications/comment", HttpMethod.PUT, updateEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(updateResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(updateResponse.body?.comments?.get(0)?.editDateTime)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteCommentTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        var responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        // Create a new comment
        val commentDto = CommentDto(
            helpPublicationId = responseHelpPublication.body?.id,
            text = "This is a test comment",
        )

        val commentHeaders = HttpHeaders()
        commentHeaders.setBearerAuth(jwtToken.toString())
        val commentEntity = HttpEntity(commentDto, commentHeaders)

        val commentResponse =
            restTemplate.exchange("/helpPublications/comment", HttpMethod.POST, commentEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(commentResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(commentResponse.body?.comments?.size, 1)

        val deleteComment = commentResponse.body?.comments?.get(0)
        deleteComment?.helpPublicationId = responseHelpPublication.body?.id

        val deleteEntity = HttpEntity(deleteComment, commentHeaders)
        val deleteResponse = restTemplate.exchange("/helpPublications/comment", HttpMethod.DELETE, deleteEntity, HelpPublicationDto::class.java)
        Assertions.assertEquals(deleteResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(deleteResponse.body?.comments?.size, 0)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getHelpPublicationsBySearchFilterTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser.body?.get("token"))
        Assertions.assertNotNull(responseUser.body?.get("user"))

        val jwtToken = responseUser.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        val responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        // Create a search filter
        val searchFilterDto = HelpPublicationSearchFilterDto(
            tags = helpPublicationDto.tags,
            initialDate = LocalDateTime.now().minusDays(1).toString(),
            finalDate = LocalDateTime.now().plusDays(1).toString(),
        )

        val searchHeaders = HttpHeaders()
        searchHeaders.setBearerAuth(jwtToken.toString())

        val uriBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost:$port/helpPublications/search")
            .queryParam("userUsername", searchFilterDto.userUsername)
            .queryParam("title", searchFilterDto.title)
            .queryParam("tags", searchFilterDto.tags?.joinToString(","))
            .queryParam("initialDate", searchFilterDto.initialDate)
            .queryParam("finalDate", searchFilterDto.finalDate)

        val uri = uriBuilder.toUriString()
        val searchEntity = HttpEntity<String>(searchHeaders)

        val searchResponse = restTemplate.exchange("/helpPublications/search", HttpMethod.GET, searchEntity, List::class.java)
        Assertions.assertEquals(searchResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(searchResponse.body?.size, 1)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun confirmAidTest() {
        val userDto = UserDto(
            "12345678A",
            "Francisco José",
            "Jordán Jiménez",
            "fjjj0001",
            "fjjj0001@red.ujaen.es",
            "666778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val user2Dto = UserDto(
            "22345678A",
            "Francisco",
            "Jordán",
            "fjjj0002",
            "fjjj0002@red.ujaen.es",
            "676778899",
            LocalDate.of(2001, 11, 19),
            "Password2@",
            null,
            mutableListOf()
        )

        val helpPublicationDto = HelpPublicationDto(
            title = "Test Publication",
            description = "Test publication created for testing purposes",
            media = mutableListOf(),
            tags = mutableListOf("test", "publication", "tags"),
            publicationDate = LocalDateTime.now(),
        )

        // User needs to be registered before creating a help publication
        val responseUser = restTemplate.postForEntity("/user/", userDto, Map::class.java)
        Assertions.assertEquals(responseUser.statusCode.is2xxSuccessful, true)

        val responseUser2 = restTemplate.postForEntity("/user/", user2Dto, Map::class.java)
        Assertions.assertEquals(responseUser2.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(responseUser2.body?.get("token"))
        Assertions.assertNotNull(responseUser2.body?.get("user"))

        val jwtToken = responseUser2.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val body = MultipartBodyBuilder()
        body.part("title", helpPublicationDto.title!!)
        body.part("description", helpPublicationDto.description!!)
        body.part("media", media)
        body.part("tags", helpPublicationDto.tags!!.joinToString(","))

        val entity = HttpEntity(body.build(), headers)

        // Create help publication
        val responseHelpPublication = restTemplate.exchange("/helpPublications/", HttpMethod.POST, entity, HelpPublicationDto::class.java)
        Assertions.assertEquals(responseHelpPublication.statusCode.is2xxSuccessful, true)

        // Create a search filter

    }
}