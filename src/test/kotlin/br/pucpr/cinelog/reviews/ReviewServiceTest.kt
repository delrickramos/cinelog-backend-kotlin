package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.movies.MovieRepository
import br.pucpr.cinelog.users.UserRepository
import br.pucpr.cinelog.users.oneMovie
import br.pucpr.cinelog.users.oneReview
import br.pucpr.cinelog.users.oneUser
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

class ReviewServiceTest {
    private val repository = mockk<ReviewRepository>()
    private val movieRepository = mockk<MovieRepository>()
    private val userRepository = mockk<UserRepository>()

    private val service = ReviewService(repository, movieRepository, userRepository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository, movieRepository, userRepository)
    }

    @Test
    fun `insert should save review when movie and user exist`() {
        val movie = oneMovie(id = 1L)
        val user = oneUser(id = 2L)
        val saved = oneReview(id = 10L, rating = 9, comment = "Great movie", user = user, movie = movie)

        every { movieRepository.findByIdOrNull(1L) } returns movie
        every { userRepository.findByIdOrNull(2L) } returns user
        every { repository.save(any()) } returns saved

        val result = service.insert(1L, 2L, 9, "Great movie")

        result shouldBe saved
    }

    @Test
    fun `insert should throw NOT_FOUND when movie does not exist`() {
        every { movieRepository.findByIdOrNull(99L) } returns null

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(99L, 1L, 8, "Nice")
        }
        ex.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `getMovieStats should calculate average and count accurately`() {
        val movie = oneMovie(id = 1L)
        val r1 = oneReview(id = 1L, rating = 10, movie = movie)
        val r2 = oneReview(id = 2L, rating = 8, movie = movie)
        every { movieRepository.findByIdOrNull(1L) } returns movie
        every { repository.findByMovieId(1L) } returns listOf(r1, r2)

        val stats = service.getMovieStats(1L)

        stats.movieId shouldBe 1L
        stats.count shouldBe 2
        stats.averageRating shouldBe 9.0
    }
}
