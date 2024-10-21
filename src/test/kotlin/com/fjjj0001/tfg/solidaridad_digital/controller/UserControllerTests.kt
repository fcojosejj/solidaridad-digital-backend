package com.fjjj0001.tfg.solidaridad_digital.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fjjj0001.tfg.solidaridad_digital.model.dto.MessageDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.UserDto
import com.fjjj0001.tfg.solidaridad_digital.model.dto.UserRatingDto
import jakarta.annotation.PostConstruct
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTests {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var jacksonConverter: MappingJackson2HttpMessageConverter

    private lateinit var restTemplate: TestRestTemplate
    private lateinit var objectMapper: ObjectMapper
    private lateinit var invalidUser: UserDto
    private lateinit var validUser: UserDto

    @PostConstruct
    fun init() {
        objectMapper = jacksonConverter.objectMapper
        val restTemplateBuilder = RestTemplateBuilder()
            .rootUri("http://localhost:$port")
            .additionalMessageConverters(jacksonConverter)

        restTemplate = TestRestTemplate(restTemplateBuilder)
    }

    @BeforeEach
    fun setup() {
        invalidUser = UserDto(
            "123B",
            "J",
            "D",
            "e",
            "no_email",
            "123",
            LocalDate.now(),
            "test",
            null,
            mutableListOf()
        )

        validUser = UserDto(
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
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun registerTest() {
        val response = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)

        val invalidResponse = restTemplate.postForEntity("/user/", invalidUser, UserDto::class.java)
        Assertions.assertEquals(invalidResponse.statusCode.is4xxClientError, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun loginTest(){
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(loginResponse.body?.get("token"))
        Assertions.assertNotNull(loginResponse.body?.get("user"))

        val responseMap = loginResponse.body as Map<*, *>
        val loggedInUserDto = objectMapper.convertValue(responseMap["user"], UserDto::class.java)

        Assertions.assertEquals(validUser.dni, loggedInUserDto.dni)
        Assertions.assertEquals(validUser.email, loggedInUserDto.email)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun logoutTest() {
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        val entity = HttpEntity<String>(headers)

        val logoutResponse = restTemplate.exchange("/user/logout", HttpMethod.POST, entity, Void::class.java)
        Assertions.assertEquals(logoutResponse.statusCode.is2xxSuccessful, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getLoggedInUserDataTest() {

        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        val entity = HttpEntity<String>(headers)

        val dataResponse = restTemplate.exchange("/user/", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(validUser.dni, dataResponse.body?.dni)

        val invalidDataResponse = restTemplate
            .withBasicAuth(validUser.email, "test")
            .getForEntity("/user/", UserDto::class.java)
        Assertions.assertEquals(invalidDataResponse.statusCode.is4xxClientError, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getUserTest(){
        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )

        val registerResponse1 = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse1.statusCode.is2xxSuccessful, true)

        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        val entity = HttpEntity<String>(headers)

        val dataResponse1 = restTemplate.exchange("/user/${validUser.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse1.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(validUser.dni, dataResponse1.body?.dni)

        val dataResponse2 = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse2.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(otherDto.dni, dataResponse2.body?.dni)

        val invalidResponse = restTemplate.exchange("/user/hello_test", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(invalidResponse.statusCode.is4xxClientError, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun updateUserTest(){
        val updatedUserDto = UserDto(
            dni = "12345678A",
            name = "Francisco",
            surname = "Jiménez",
            username = "fj_new",
            email = "fjjj0001@red.ujaen.es",
            phone = "+34677889933",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password2@",
            aidsCompleted = mutableListOf()
        )

        val registerResponse1 = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse1.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        val entity = HttpEntity(updatedUserDto, headers)

        val updateResponse = restTemplate.exchange("/user/", HttpMethod.PUT, entity, Map::class.java)
        Assertions.assertEquals(updateResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(updateResponse.body?.get("token"))
        Assertions.assertNotNull(updateResponse.body?.get("user"))

        val responseMap = updateResponse.body as Map<*, *>
        val newUserDto = objectMapper.convertValue(responseMap["user"], UserDto::class.java)

        // DNI, email and username should not be updated
        Assertions.assertEquals(validUser.dni, newUserDto.dni)
        Assertions.assertEquals(updatedUserDto.name, newUserDto.name)
        Assertions.assertEquals(updatedUserDto.surname, newUserDto.surname)
        Assertions.assertEquals(validUser.username, newUserDto.username)
        Assertions.assertEquals(validUser.email, newUserDto.email)
        Assertions.assertEquals(updatedUserDto.phone, newUserDto.phone)

        // Password update
        val updatedPwdUserDto = UserDto(
            dni = "12345678A",
            name = "Francisco",
            surname = "Jiménez",
            username = "fj_new",
            email = "fjjj0001@red.ujaen.es",
            phone = "+34677889933",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password2@",
            newPassword = "Contraseña3*",
            aidsCompleted = mutableListOf()
        )

        val pwdResponse = restTemplate.exchange("/user/", HttpMethod.PUT, entity, Map::class.java)
        Assertions.assertEquals(pwdResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertNotNull(pwdResponse.body?.get("token"))
        Assertions.assertNotNull(pwdResponse.body?.get("user"))
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteUserTest() {
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())
        val entity = HttpEntity<String>(headers)

        val deleteResponse = restTemplate.exchange("/user/", HttpMethod.DELETE, entity, Void::class.java)
        Assertions.assertEquals(deleteResponse.statusCode.is2xxSuccessful, true)

        val invalidLoginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(invalidLoginResponse.statusCode.is4xxClientError, true)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getTop10UsersByAidsCompletedTest(){
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val uriBuilder = UriComponentsBuilder.fromHttpUrl("http://localhost:$port/user/ranking")
            .queryParam("dateString", LocalDateTime.now().minusDays(1).toString())

        uriBuilder.toUriString()
        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity<String>(headers)

        val response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, entity, List::class.java)

        //The response is successful, but the list is empty because no user has completed any aid
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(response.body?.size, 0)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun addRatingTest(){
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )
        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val ratingDto = UserRatingDto(
            ratedByUsername = validUser.username,
            ratingUserUsername = otherDto.username,
            rating = 5,
            message = "Great user"
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity(ratingDto, headers)

        val response = restTemplate.exchange("/user/rating", HttpMethod.POST, entity, UserDto::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(response.body?.ratings?.size, 1)

        val dataResponse = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(otherDto.username, dataResponse.body?.username)
        Assertions.assertEquals(5, dataResponse.body?.ratings?.get(0)?.rating)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun deleteUserRatingTest(){
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )
        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val ratingDto = UserRatingDto(
            ratedByUsername = validUser.username,
            ratingUserUsername = otherDto.username,
            rating = 5,
            message = "Great user"
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity(ratingDto, headers)

        val response = restTemplate.exchange("/user/rating", HttpMethod.POST, entity, UserDto::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(response.body?.ratings?.size, 1)

        val dataResponse = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(otherDto.username, dataResponse.body?.username)
        Assertions.assertEquals(5, dataResponse.body?.ratings?.get(0)?.rating)

        val deleteEntity = HttpEntity<String>(headers)
        val deleteResponse = restTemplate.exchange("/user/rating/${otherDto.username}/${dataResponse.body?.ratings?.get(0)?.id}", HttpMethod.DELETE, deleteEntity, Void::class.java)
        Assertions.assertEquals(deleteResponse.statusCode.is2xxSuccessful, true)

        val dataResponse2 = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse2.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(0, dataResponse2.body?.ratings?.size)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun receiveMessageTest() {
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )

        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val messageDto = MessageDto(
            senderUsername = validUser.username,
            receiverUsername = otherDto.username,
            message = "Hellooo!!"
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity(messageDto, headers)

        val response = restTemplate.exchange("/user/messages", HttpMethod.POST, entity, Void::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)

        val dataResponse = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(dataResponse.body?.receivedMessages?.size, 1)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getChatListTest() {
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )

        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        val messageDto = MessageDto(
            senderUsername = validUser.username,
            receiverUsername = otherDto.username,
            message = "Hellooo!!"
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity(messageDto, headers)

        val response = restTemplate.exchange("/user/messages", HttpMethod.POST, entity, Void::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)

        val dataResponse = restTemplate.exchange("/user/${otherDto.username}", HttpMethod.GET, entity, UserDto::class.java)
        Assertions.assertEquals(dataResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(dataResponse.body?.receivedMessages?.size, 1)

        val chatListResponse = restTemplate.exchange("/user/messages", HttpMethod.GET, HttpEntity<String>(headers), List::class.java)
        Assertions.assertEquals(chatListResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(chatListResponse.body?.size, 1)
        Assertions.assertEquals(chatListResponse.body?.get(0), otherDto.username)
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    fun getChatTest() {
        val registerResponse = restTemplate.postForEntity("/user/", validUser, UserDto::class.java)
        Assertions.assertEquals(registerResponse.statusCode.is2xxSuccessful, true)

        val otherDto = UserDto(
            dni = "12345678B",
            name = "Francisco José",
            surname = "Jordán Jiménez",
            username = "fjjj0002",
            email = "fjjj0002@red.ujaen.es",
            phone = "+34677889922",
            birthdate = LocalDate.of(2001, 11, 19),
            password = "Password1234@",
            aidsCompleted = mutableListOf()
        )

        val registerResponse2 = restTemplate.postForEntity("/user/", otherDto, UserDto::class.java)
        Assertions.assertEquals(registerResponse2.statusCode.is2xxSuccessful, true)

        val loginResponse = restTemplate.postForEntity("/user/login", validUser, Map::class.java)
        Assertions.assertEquals(loginResponse.statusCode.is2xxSuccessful, true)
        val jwtToken = loginResponse.body?.get("token")

        var messageDto = MessageDto(
            senderUsername = validUser.username,
            receiverUsername = otherDto.username,
            message = "Hellooo!!"
        )

        val headers = HttpHeaders()
        headers.setBearerAuth(jwtToken.toString())

        val entity = HttpEntity(messageDto, headers)

        val response = restTemplate.exchange("/user/messages", HttpMethod.POST, entity, Void::class.java)
        Assertions.assertEquals(response.statusCode.is2xxSuccessful, true)

        messageDto = MessageDto(
            senderUsername = otherDto.username,
            receiverUsername = validUser.username,
            message = "Hiii!!"
        )

        val entity2 = HttpEntity(messageDto, headers)
        val response2 = restTemplate.exchange("/user/messages", HttpMethod.POST, entity2, Void::class.java)
        Assertions.assertEquals(response2.statusCode.is2xxSuccessful, true)

        messageDto = MessageDto(
            senderUsername = validUser.username,
            receiverUsername = otherDto.username,
            message = "How are you?"
        )

        val entity3 = HttpEntity(messageDto, headers)
        val response3 = restTemplate.exchange("/user/messages", HttpMethod.POST, entity3, Void::class.java)
        Assertions.assertEquals(response3.statusCode.is2xxSuccessful, true)

        val chatResponse = restTemplate.exchange("/user/messages/${otherDto.username}", HttpMethod.GET, HttpEntity<String>(headers), List::class.java)
        Assertions.assertEquals(chatResponse.statusCode.is2xxSuccessful, true)
        Assertions.assertEquals(chatResponse.body?.size, 2)
    }
}