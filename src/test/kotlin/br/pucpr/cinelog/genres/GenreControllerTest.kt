package br.pucpr.cinelog.genres

import br.pucpr.cinelog.users.InvalidSortDirException
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneGenre
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

class GenreControllerTest {
    private val service = mockk<GenreService>()
    private val controller = GenreController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should return 200 OK with genres ordered by sortDir`() {
        val g1 = oneGenre(id = 1L, name = "Adventure")
        val g2 = oneGenre(id = 2L, name = "Sci-Fi")
        every { service.findAll(SortDir.ASC) } returns listOf(g1, g2)

        val response = controller.list(sortDir = null)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(GenreResponse(g1), GenreResponse(g2))
    }

    @Test
    fun `list should throw InvalidSortDirException for invalid sortDir`() {
        shouldThrow<InvalidSortDirException> {
            controller.list(sortDir = "WRONG")
        }
    }

    @Test
    fun `getById should return 200 OK when genre exists`() {
        val genre = oneGenre(id = 1L)
        every { service.findByIdOrNull(1L) } returns genre

        val response = controller.getById(1L)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe GenreResponse(genre)
    }

    @Test
    fun `getById should return 404 NOT_FOUND when genre does not exist`() {
        every { service.findByIdOrNull(99L) } returns null

        val response = controller.getById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldBe null
    }

    @Test
    fun `insert should return 201 CREATED with saved genre`() {
        val request = GenreRequest(name = "Thriller", description = "Suspenseful stories")
        val saved = oneGenre(id = 3L, name = "Thriller", description = "Suspenseful stories")
        every { service.insert(any()) } returns saved

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe GenreResponse(saved)
        verify(exactly = 1) { service.insert(match { it.name == "Thriller" }) }
    }

    @Test
    fun `update should return 200 OK when description changed`() {
        val request = PatchGenreRequest(description = "Updated description")
        val updated = oneGenre(id = 1L, description = "Updated description")
        every { service.update(1L, "Updated description") } returns updated

        val response = controller.update(1L, request)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe GenreResponse(updated)
    }

    @Test
    fun `update should return 204 NO_CONTENT when description did not change`() {
        val request = PatchGenreRequest(description = "Science Fiction")
        every { service.update(1L, "Science Fiction") } returns null

        val response = controller.update(1L, request)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        response.body shouldBe null
    }

    @Test
    fun `deleteById should return 200 OK when genre was deleted`() {
        every { service.deleteById(1L) } returns true

        val response = controller.deleteById(1L)

        response.statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `deleteById should return 404 NOT_FOUND when genre does not exist`() {
        every { service.deleteById(99L) } returns false

        val response = controller.deleteById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }
}
