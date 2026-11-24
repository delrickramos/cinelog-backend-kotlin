package br.pucpr.cinelog.users

import br.pucpr.cinelog.users.requests.LoginRequest
import br.pucpr.cinelog.users.requests.PatchUserRequest
import br.pucpr.cinelog.users.requests.UserProfileRequest
import br.pucpr.cinelog.users.requests.UserRequest
import br.pucpr.cinelog.users.responses.LoginResponse
import br.pucpr.cinelog.users.responses.UserResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException

class UserControllerTest {
    private val service = mockk<UserService>()
    private val controller = UserController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should filter by role when role parameter is provided`() {
        val user = oneUser(id = 1L)
        every { service.findByRole("ADMIN") } returns listOf(user)

        val response = controller.list(sortDir = null, role = "ADMIN")

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(UserResponse(user))
    }

    @Test
    fun `list should sort users when role is null`() {
        val user = oneUser(id = 1L)
        every { service.findAll(SortDir.DESC) } returns listOf(user)

        val response = controller.list(sortDir = "DESC", role = null)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(UserResponse(user))
    }

    @Test
    fun `insert should return 201 CREATED with created user`() {
        val request = UserRequest(email = "clara@cinelog.com", password = "secret", name = "Clara")
        val saved = oneUser(id = 3L, email = "clara@cinelog.com", name = "Clara")
        every { service.insert(any()) } returns saved

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe UserResponse(saved)
    }

    @Test
    fun `login should return 200 OK when credentials are valid`() {
        val user = oneUser(id = 1L, email = "john@cinelog.com")
        val loginResponse = LoginResponse(token = "jwt-token", user = UserResponse(user))
        every { service.login("john@cinelog.com", "1234") } returns loginResponse

        val response = controller.login(LoginRequest(email = "john@cinelog.com", password = "1234"))

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe loginResponse
    }

    @Test
    fun `login should return 401 UNAUTHORIZED when credentials are invalid`() {
        every { service.login("john@cinelog.com", "wrong") } returns null

        val response = controller.login(LoginRequest(email = "john@cinelog.com", password = "wrong"))

        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
        response.body shouldBe null
    }

    @Test
    fun `update should return 200 OK when name was updated by owner`() {
        val requester = oneUserToken(id = 1L)
        val updated = oneUser(id = 1L, name = "Updated Name")
        every { service.update(1L, "Updated Name", requester) } returns updated

        val response = controller.update(1L, PatchUserRequest(name = "Updated Name"), requester)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe UserResponse(updated)
    }

    @Test
    fun `update should return 204 NO_CONTENT when name did not change`() {
        val requester = oneUserToken(id = 1L)
        every { service.update(1L, "John Doe", requester) } returns null

        val response = controller.update(1L, PatchUserRequest(name = "John Doe"), requester)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        response.body shouldBe null
    }

    @Test
    fun `updateProfile should return 200 OK with updated profile`() {
        val requester = oneUserToken(id = 1L)
        val profileRequest = UserProfileRequest(
            bio = "Cinema lover",
            avatarUrl = "https://example.com/avatar.png",
            favoriteMovieQuote = "There's no place like home."
        )
        val updatedUser = oneUser(
            id = 1L,
            profile = UserProfile(
                id = 10L,
                bio = "Cinema lover",
                avatarUrl = "https://example.com/avatar.png",
                favoriteMovieQuote = "There's no place like home."
            )
        )
        every { service.updateProfile(1L, any(), requester) } returns updatedUser

        val response = controller.updateProfile(1L, profileRequest, requester)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe UserResponse(updatedUser)
    }

    @Test
    fun `updateProfile should propagate AccessDeniedException when non-admin updates another user's profile`() {
        val otherUser = oneUserToken(id = 2L)
        val profileRequest = UserProfileRequest(bio = "Unauthorized")
        every { service.updateProfile(1L, any(), otherUser) } throws AccessDeniedException("Forbidden")

        shouldThrow<AccessDeniedException> {
            controller.updateProfile(1L, profileRequest, otherUser)
        }
    }

    @Test
    fun `getById should return 200 OK when user exists and 404 NOT_FOUND otherwise`() {
        val user = oneUser(id = 1L)
        every { service.findByIdOrNull(1L) } returns user
        every { service.findByIdOrNull(99L) } returns null

        val found = controller.getById(1L)
        val missing = controller.getById(99L)

        found.statusCode shouldBe HttpStatus.OK
        found.body shouldBe UserResponse(user)
        missing.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `deleteById should return 200 OK when deleted and 404 NOT_FOUND when missing`() {
        every { service.deleteById(1L) } returns true
        every { service.deleteById(99L) } returns false

        controller.deleteById(1L).statusCode shouldBe HttpStatus.OK
        controller.deleteById(99L).statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `grant should return 200 OK when role added and 204 NO_CONTENT when already granted`() {
        every { service.addRole(1L, "ADMIN") } returns true
        every { service.addRole(1L, "PREMIUM") } returns false

        controller.grant(1L, "ADMIN").statusCode shouldBe HttpStatus.OK
        controller.grant(1L, "PREMIUM").statusCode shouldBe HttpStatus.NO_CONTENT
    }
}
