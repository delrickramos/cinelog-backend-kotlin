package br.pucpr.cinelog.movies

import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.users.oneDirector
import br.pucpr.cinelog.users.oneGenre
import br.pucpr.cinelog.users.oneMovie
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class MovieServiceTest {
    private val repository = mockk<MovieRepository>()
    private val directorRepository = mockk<DirectorRepository>()
    private val genreRepository = mockk<GenreRepository>()

    private val service = MovieService(repository, directorRepository, genreRepository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository, directorRepository, genreRepository)
    }

    @Test
    fun `insert should save movie and associate director when directorId is provided`() {
        val director = oneDirector(id = 10L)
        val movie = oneMovie(id = null, director = null)
        val saved = oneMovie(id = 1L, director = director)

        every { repository.findByTitleIgnoreCase(movie.title) } returns null
        every { directorRepository.findByIdOrNull(10L) } returns director
        every { repository.save(movie) } returns saved

        val result = service.insert(movie, directorId = 10L)

        result shouldBe saved
        movie.director shouldBe director
    }

    @Test
    fun `insert should throw CONFLICT if movie title already exists`() {
        val movie = oneMovie(id = null)
        every { repository.findByTitleIgnoreCase(movie.title) } returns oneMovie(id = 1L)

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(movie)
        }
        ex.statusCode shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `addGenre should associate genre and return true when not already present`() {
        val movie = oneMovie(id = 1L, genres = mutableSetOf())
        val genre = oneGenre(id = 2L, name = "Drama")
        every { repository.findByIdOrNull(1L) } returns movie
        every { genreRepository.findByNameIgnoreCase("Drama") } returns genre
        every { repository.save(movie) } returns movie

        service.addGenre(1L, "Drama") shouldBe true
        movie.genres.contains(genre) shouldBe true
    }
}
