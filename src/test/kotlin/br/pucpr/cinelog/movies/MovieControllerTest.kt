package br.pucpr.cinelog.movies

import br.pucpr.cinelog.movies.requests.MovieRequest
import br.pucpr.cinelog.movies.requests.PatchMovieRequest
import br.pucpr.cinelog.movies.responses.MovieResponse
import br.pucpr.cinelog.users.InvalidSortDirException
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneMovie
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

class MovieControllerTest {
    private val service = mockk<MovieService>()
    private val controller = MovieController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should forward filters and sortDir to service and return 200 OK`() {
        val movie = oneMovie(id = 1L, title = "Interstellar")
        every { service.findAll("Sci-Fi", 2L, 2010, "Inter", SortDir.DESC) } returns listOf(movie)

        val response = controller.list(
            genre = "Sci-Fi",
            directorId = 2L,
            minYear = 2010,
            title = "Inter",
            sortDir = "DESC"
        )

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(MovieResponse(movie))
    }

    @Test
    fun `list should throw InvalidSortDirException for invalid sortDir`() {
        shouldThrow<InvalidSortDirException> {
            controller.list(null, null, null, null, "BAD")
        }
    }

    @Test
    fun `getById should return 200 OK when movie exists`() {
        val movie = oneMovie(id = 1L)
        every { service.findByIdOrNull(1L) } returns movie

        val response = controller.getById(1L)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe MovieResponse(movie)
    }

    @Test
    fun `getById should return 404 NOT_FOUND when movie does not exist`() {
        every { service.findByIdOrNull(99L) } returns null

        val response = controller.getById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldBe null
    }

    @Test
    fun `insert should return 201 CREATED with saved movie`() {
        val request = MovieRequest(
            title = "Arrival",
            synopsis = "A linguist works with the military to communicate with alien lifeforms.",
            releaseYear = 2016,
            durationMinutes = 116,
            posterUrl = "https://example.com/arrival.jpg",
            directorId = 2L
        )
        val saved = oneMovie(id = 10L, title = "Arrival", releaseYear = 2016, durationMinutes = 116)
        every { service.insert(any(), 2L) } returns saved

        val response = controller.insert(request)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe MovieResponse(saved)
        verify(exactly = 1) { service.insert(match { it.title == "Arrival" }, 2L) }
    }

    @Test
    fun `update should return 200 OK with updated movie`() {
        val request = MovieRequest(
            title = "Interstellar (IMAX)",
            synopsis = "Updated synopsis for Interstellar movie.",
            releaseYear = 2014,
            durationMinutes = 169,
            directorId = 1L
        )
        val updated = oneMovie(id = 1L, title = "Interstellar (IMAX)")
        every { service.update(1L, any(), 1L) } returns updated

        val response = controller.update(1L, request)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe MovieResponse(updated)
    }

    @Test
    fun `patch should return 200 OK when movie was modified`() {
        val request = PatchMovieRequest(title = "Interstellar Special Edition")
        val patched = oneMovie(id = 1L, title = "Interstellar Special Edition")
        every { service.patch(1L, "Interstellar Special Edition", null, null, null, null) } returns patched

        val response = controller.patch(1L, request)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe MovieResponse(patched)
    }

    @Test
    fun `patch should return 204 NO_CONTENT when nothing changed`() {
        val request = PatchMovieRequest(title = "Interstellar")
        every { service.patch(1L, "Interstellar", null, null, null, null) } returns null

        val response = controller.patch(1L, request)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        response.body shouldBe null
    }

    @Test
    fun `deleteById should return 200 OK when movie was deleted`() {
        every { service.deleteById(1L) } returns true

        val response = controller.deleteById(1L)

        response.statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `deleteById should return 404 NOT_FOUND when movie does not exist`() {
        every { service.deleteById(99L) } returns false

        val response = controller.deleteById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `addGenre should return 200 OK when genre added and 204 NO_CONTENT when already associated`() {
        every { service.addGenre(1L, "Drama") } returns true
        every { service.addGenre(1L, "Sci-Fi") } returns false

        controller.addGenre(1L, "Drama").statusCode shouldBe HttpStatus.OK
        controller.addGenre(1L, "Sci-Fi").statusCode shouldBe HttpStatus.NO_CONTENT
    }

    @Test
    fun `removeGenre should return 200 OK when removed and 204 NO_CONTENT when not associated`() {
        every { service.removeGenre(1L, "Sci-Fi") } returns true
        every { service.removeGenre(1L, "Horror") } returns false

        controller.removeGenre(1L, "Sci-Fi").statusCode shouldBe HttpStatus.OK
        controller.removeGenre(1L, "Horror").statusCode shouldBe HttpStatus.NO_CONTENT
    }

    @Test
    fun `setDirector should return 200 OK when changed and 204 NO_CONTENT when already assigned`() {
        every { service.setDirector(1L, 2L) } returns true
        every { service.setDirector(1L, 1L) } returns false

        controller.setDirector(1L, 2L).statusCode shouldBe HttpStatus.OK
        controller.setDirector(1L, 1L).statusCode shouldBe HttpStatus.NO_CONTENT
    }

    @Test
    fun `removeDirector should return 200 OK when disassociated and 204 NO_CONTENT when already null`() {
        every { service.removeDirector(1L) } returns true
        every { service.removeDirector(2L) } returns false

        controller.removeDirector(1L).statusCode shouldBe HttpStatus.OK
        controller.removeDirector(2L).statusCode shouldBe HttpStatus.NO_CONTENT
    }
}
