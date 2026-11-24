package br.pucpr.cinelog.roles

import br.pucpr.cinelog.users.oneRole
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class RoleControllerTest {
    private val service = mockk<RoleService>()
    private val controller = RoleController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should return 200 OK with roles mapped to response`() {
        val r1 = oneRole(id = 1L, name = "ADMIN", description = "System Administrator")
        val r2 = oneRole(id = 2L, name = "PREMIUM", description = "Premium Reviewer")
        every { service.findAll() } returns listOf(r1, r2)

        val response = controller.list()

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(RoleResponse(r1), RoleResponse(r2))
    }

    @Test
    fun `insert should return 201 CREATED with saved role`() {
        val request = RoleRequest(name = "CURATOR", description = "Catalog Curator")
        val saved = oneRole(id = 3L, name = "CURATOR", description = "Catalog Curator")
        every { service.insert(any()) } returns saved

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe RoleResponse(saved)
        verify(exactly = 1) { service.insert(match { it.name == "CURATOR" }) }
    }
}
