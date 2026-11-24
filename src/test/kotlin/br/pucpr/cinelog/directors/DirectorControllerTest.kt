package br.pucpr.cinelog.directors

import br.pucpr.cinelog.directors.requests.DirectorRequest
import br.pucpr.cinelog.directors.requests.PatchDirectorRequest
import br.pucpr.cinelog.directors.responses.DirectorResponse
import br.pucpr.cinelog.users.InvalidSortDirException
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneDirector
import io.kotest.assertions.throwables.shouldThrow
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

class DirectorControllerTest {
    private val service = mockk<DirectorService>()
    private val controller = DirectorController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should return 200 OK with directors mapped to response`() {
        val d1 = oneDirector(id = 1L, name = "Christopher Nolan")
        val d2 = oneDirector(id = 2L, name = "Denis Villeneuve", nationality = "Canadian")
        every { service.findAll("Canadian", SortDir.DESC) } returns listOf(d2, d1)

        val response = controller.list(nationality = "Canadian", sortDir = "DESC")

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(DirectorResponse(d2), DirectorResponse(d1))
    }

    @Test
    fun `list should default sortDir to ASC when null`() {
        val d1 = oneDirector(id = 1L)
        every { service.findAll(null, SortDir.ASC) } returns listOf(d1)

        val response = controller.list(nationality = null, sortDir = null)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(DirectorResponse(d1))
    }

    @Test
    fun `list should throw InvalidSortDirException for invalid sortDir`() {
        shouldThrow<InvalidSortDirException> {
            controller.list(nationality = null, sortDir = "INVALID")
        }
    }

    @Test
    fun `getById should return 200 OK when director exists`() {
        val director = oneDirector(id = 1L)
        every { service.findByIdOrNull(1L) } returns director

        val response = controller.getById(1L)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe DirectorResponse(director)
    }

    @Test
    fun `getById should return 404 NOT_FOUND when director does not exist`() {
        every { service.findByIdOrNull(99L) } returns null

        val response = controller.getById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldBe null
    }

    @Test
    fun `insert should return 201 CREATED with created director`() {
        val request = DirectorRequest(
            name = "Hayao Miyazaki",
            nationality = "Japanese",
            birthYear = 1941,
            biography = "Studio Ghibli co-founder"
        )
        val saved = oneDirector(
            id = 5L,
            name = "Hayao Miyazaki",
            nationality = "Japanese",
            birthYear = 1941,
            biography = "Studio Ghibli co-founder"
        )
        every { service.insert(any()) } returns saved

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe DirectorResponse(saved)
        verify(exactly = 1) { service.insert(match { it.name == "Hayao Miyazaki" && it.birthYear == 1941 }) }
    }

    @Test
    fun `update should return 200 OK with updated director`() {
        val request = DirectorRequest(
            name = "Christopher Nolan",
            nationality = "British",
            birthYear = 1970,
            biography = "Updated bio"
        )
        val updated = oneDirector(id = 1L, nationality = "British", biography = "Updated bio")
        every { service.update(1L, any()) } returns updated

        val response = controller.update(1L, request)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe DirectorResponse(updated)
    }

    @Test
    fun `patch should return 200 OK when director was modified`() {
        val request = PatchDirectorRequest(biography = "New bio")
        val patched = oneDirector(id = 1L, biography = "New bio")
        every { service.patch(1L, null, null, "New bio") } returns patched

        val response = controller.patch(1L, request)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe DirectorResponse(patched)
    }

    @Test
    fun `patch should return 204 NO_CONTENT when nothing changed`() {
        val request = PatchDirectorRequest(name = "Christopher Nolan")
        every { service.patch(1L, "Christopher Nolan", null, null) } returns null

        val response = controller.patch(1L, request)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        response.body shouldBe null
    }

    @Test
    fun `deleteById should return 200 OK when director was deleted`() {
        every { service.deleteById(1L) } returns true

        val response = controller.deleteById(1L)

        response.statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `deleteById should return 404 NOT_FOUND when director does not exist`() {
        every { service.deleteById(99L) } returns false

        val response = controller.deleteById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }
}
