package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.movies.MovieRepository
import br.pucpr.cinelog.users.UserRepository
import br.pucpr.cinelog.users.oneAdminToken
import br.pucpr.cinelog.users.oneMovie
import br.pucpr.cinelog.users.oneReview
import br.pucpr.cinelog.users.oneUser
import br.pucpr.cinelog.users.oneUserToken
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
import org.springframework.security.access.AccessDeniedException
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
    fun `insert should save review when movie and user exist and requester is owner`() {
        val movie = oneMovie(id = 1L)
        val user = oneUser(id = 2L)
        val requester = oneUserToken(id = 2L)
        val saved = oneReview(id = 10L, rating = 9, comment = "Great movie", user = user, movie = movie)

        every { movieRepository.findByIdOrNull(1L) } returns movie
        every { userRepository.findByIdOrNull(2L) } returns user
        every { repository.save(any()) } returns saved

        val result = service.insert(1L, 2L, 9, "Great movie", requester)

        result shouldBe saved
    }

    @Test
    fun `insert should throw AccessDeniedException when non-admin tries to create review for another user`() {
        val requester = oneUserToken(id = 3L)

        shouldThrow<AccessDeniedException> {
            service.insert(1L, 2L, 9, "Impersonation attempt", requester)
        }
    }

    @Test
    fun `insert should allow ADMIN to create review for another user`() {
        val movie = oneMovie(id = 1L)
        val user = oneUser(id = 2L)
        val admin = oneAdminToken(id = 99L)
        val saved = oneReview(id = 10L, rating = 8, comment = "Admin curated", user = user, movie = movie)

        every { movieRepository.findByIdOrNull(1L) } returns movie
        every { userRepository.findByIdOrNull(2L) } returns user
        every { repository.save(any()) } returns saved

        val result = service.insert(1L, 2L, 8, "Admin curated", admin)

        result shouldBe saved
    }

    @Test
    fun `insert should throw NOT_FOUND when movie does not exist`() {
        val requester = oneUserToken(id = 1L)
        every { movieRepository.findByIdOrNull(99L) } returns null

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(99L, 1L, 8, "Nice", requester)
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

    @Test
    fun `patch should update review when requester is owner`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, rating = 7, comment = "Old comment", user = author)
        every { repository.findByIdOrNull(10L) } returns review
        every { repository.save(review) } returns review

        val updated = service.patch(10L, 9, "New comment", oneUserToken(id = 2L))

        updated?.rating shouldBe 9
        updated?.comment shouldBe "New comment"
    }

    @Test
    fun `patch should throw AccessDeniedException when requester is not owner nor ADMIN`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, rating = 7, comment = "Old comment", user = author)
        every { repository.findByIdOrNull(10L) } returns review

        shouldThrow<AccessDeniedException> {
            service.patch(10L, 1, "Hacked", oneUserToken(id = 3L))
        }
    }

    @Test
    fun `patch should allow ADMIN to update another user's review`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, rating = 7, comment = "Old comment", user = author)
        every { repository.findByIdOrNull(10L) } returns review
        every { repository.save(review) } returns review

        val updated = service.patch(10L, 8, "Moderated comment", oneAdminToken(id = 99L))

        updated?.rating shouldBe 8
        updated?.comment shouldBe "Moderated comment"
    }

    @Test
    fun `deleteById should delete review when requester is owner`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, user = author)
        every { repository.findByIdOrNull(10L) } returns review
        justRun { repository.delete(review) }

        service.deleteById(10L, oneUserToken(id = 2L)) shouldBe true
        verify(exactly = 1) { repository.delete(review) }
    }

    @Test
    fun `deleteById should delete review when requester is ADMIN`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, user = author)
        every { repository.findByIdOrNull(10L) } returns review
        justRun { repository.delete(review) }

        service.deleteById(10L, oneAdminToken(id = 99L)) shouldBe true
        verify(exactly = 1) { repository.delete(review) }
    }

    @Test
    fun `deleteById should throw AccessDeniedException when requester is not owner nor ADMIN`() {
        val author = oneUser(id = 2L)
        val review = oneReview(id = 10L, user = author)
        every { repository.findByIdOrNull(10L) } returns review

        shouldThrow<AccessDeniedException> {
            service.deleteById(10L, oneUserToken(id = 3L))
        }
    }

    @Test
    fun `deleteById should return false when review does not exist`() {
        every { repository.findByIdOrNull(999L) } returns null

        service.deleteById(999L, oneUserToken(id = 2L)) shouldBe false
    }
}
