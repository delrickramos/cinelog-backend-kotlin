package br.pucpr.cinelog.users

import br.pucpr.cinelog.roles.Role
import br.pucpr.cinelog.roles.RoleRepository
import br.pucpr.cinelog.security.JWT
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserServiceTest {
    private val repository = mockk<UserRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val jwt = mockk<JWT>()

    private val service = UserService(repository, roleRepository, jwt)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository, roleRepository, jwt)
    }

    @Test
    fun `insert should save and return the saved user`() {
        val user = oneUser(id = null)
        val saved = oneUser(id = 1)
        every { repository.findByEmail(user.email) } returns null
        every { repository.save(user) } returns saved

        val result = service.insert(user)

        verify(exactly = 1) { repository.findByEmail(user.email) }
        result shouldBe saved
    }

    @Test
    fun `insert should throw CONFLICT when email already exists`() {
        val user = oneUser(id = null)
        every { repository.findByEmail(user.email) } returns oneUser(id = 1)

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(user)
        }
        ex.statusCode shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `deleteById should throw BAD_REQUEST when deleting the last ADMIN`() {
        val adminRole = Role(id = 1, name = "ADMIN", description = "Admin")
        val admin = oneUser(id = 1, roles = mutableSetOf(adminRole))
        every { repository.findByIdOrNull(1L) } returns admin
        every { repository.findByRole("ADMIN") } returns listOf(admin)

        val ex = shouldThrow<ResponseStatusException> {
            service.deleteById(1L)
        }
        ex.statusCode shouldBe HttpStatus.BAD_REQUEST
    }

    @Test
    fun `deleteById should delete non-admin user and return true`() {
        val user = oneUser(id = 2)
        every { repository.findByIdOrNull(2L) } returns user
        justRun { repository.delete(user) }

        service.deleteById(2L) shouldBe true
        verify(exactly = 1) { repository.delete(user) }
    }

    @Test
    fun `login should return token and user when credentials match`() {
        val user = oneUser(id = 1, password = "secret")
        every { repository.findByEmail(user.email) } returns user
        every { jwt.createToken(user) } returns "jwt-token-123"

        val response = service.login(user.email, "secret")

        response?.token shouldBe "jwt-token-123"
        response?.user?.id shouldBe 1L
    }
}
