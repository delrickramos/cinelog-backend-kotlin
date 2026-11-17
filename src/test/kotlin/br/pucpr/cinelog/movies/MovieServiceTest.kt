package br.pucpr.cinelog.movies

import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneDirector
import br.pucpr.cinelog.users.oneGenre
import br.pucpr.cinelog.users.oneMovie
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
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
    fun `findAll should filter by minYear and title and sort descending`() {
        val m1 = oneMovie(id = 1L, title = "Arrival", releaseYear = 2016)
        val m2 = oneMovie(id = 2L, title = "Dune", releaseYear = 2021)
        val m3 = oneMovie(id = 3L, title = "Alien", releaseYear = 1979)
        every { repository.findAll() } returns listOf(m1, m2, m3)

        val result = service.findAll(minYear = 2000, dir = SortDir.DESC)

        result.map { it.title } shouldContainExactly listOf("Dune", "Arrival")
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

    @Test
    fun `addGenre should return false when movie already has genre`() {
        val genre = oneGenre(id = 1L, name = "Sci-Fi")
        val movie = oneMovie(id = 1L, genres = mutableSetOf(genre))
        every { repository.findByIdOrNull(1L) } returns movie
        every { genreRepository.findByNameIgnoreCase("Sci-Fi") } returns genre

        service.addGenre(1L, "Sci-Fi") shouldBe false
    }

    @Test
    fun `removeGenre should remove genre and return true`() {
        val genre = oneGenre(id = 1L, name = "Sci-Fi")
        val movie = oneMovie(id = 1L, genres = mutableSetOf(genre))
        every { repository.findByIdOrNull(1L) } returns movie
        every { repository.save(movie) } returns movie

        service.removeGenre(1L, "Sci-Fi") shouldBe true
        movie.genres.isEmpty() shouldBe true
    }

    @Test
    fun `deleteById should delete movie when found`() {
        val movie = oneMovie(id = 1L)
        every { repository.findByIdOrNull(1L) } returns movie
        justRun { repository.delete(movie) }

        service.deleteById(1L) shouldBe true
        verify(exactly = 1) { repository.delete(movie) }
    }
}
