package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.reviews.requests.PatchReviewRequest
import br.pucpr.cinelog.reviews.requests.ReviewRequest
import br.pucpr.cinelog.reviews.responses.ReviewResponse
import br.pucpr.cinelog.reviews.responses.ReviewStatsResponse
import br.pucpr.cinelog.users.InvalidSortDirException
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneAdminToken
import br.pucpr.cinelog.users.oneReview
import br.pucpr.cinelog.users.oneUser
import br.pucpr.cinelog.users.oneUserToken
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

class ReviewControllerTest {
    private val service = mockk<ReviewService>()
    private val controller = ReviewController(service)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(service)
    }

    @Test
    fun `list should default sortDir to DESC and return 200 OK`() {
        val review = oneReview(id = 1L, rating = 9)
        every { service.findAll(1L, 2L, 8, SortDir.DESC) } returns listOf(review)

        val response = controller.list(movieId = 1L, userId = 2L, minRating = 8, sortDir = null)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(ReviewResponse(review))
    }

    @Test
    fun `list should throw InvalidSortDirException for invalid sortDir`() {
        shouldThrow<InvalidSortDirException> {
            controller.list(null, null, null, "INVALID")
        }
    }

    @Test
    fun `getById should return 200 OK when review exists`() {
        val review = oneReview(id = 1L)
        every { service.findByIdOrNull(1L) } returns review

        val response = controller.getById(1L)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe ReviewResponse(review)
    }

    @Test
    fun `getById should return 404 NOT_FOUND when review does not exist`() {
        every { service.findByIdOrNull(99L) } returns null

        val response = controller.getById(99L)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
        response.body shouldBe null
    }

    @Test
    fun `listByMovie should return 200 OK with movie reviews`() {
        val review = oneReview(id = 1L)
        every { service.findAll(movieId = 5L, dir = SortDir.ASC) } returns listOf(review)

        val response = controller.listByMovie(movieId = 5L, sortDir = "ASC")

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe listOf(ReviewResponse(review))
    }

    @Test
    fun `statsByMovie should return 200 OK with review stats`() {
        val stats = ReviewStatsResponse(movieId = 1L, count = 2, averageRating = 9.5)
        every { service.getMovieStats(1L) } returns stats

        val response = controller.statsByMovie(1L)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe stats
    }

    @Test
    fun `insert should use authenticated user id when userId is omitted and return 201 CREATED`() {
        val requester = oneUserToken(id = 2L)
        val request = ReviewRequest(userId = null, rating = 9, comment = "Incredible cinema!")
        val saved = oneReview(id = 10L, rating = 9, comment = "Incredible cinema!", user = oneUser(id = 2L))
        every { service.insert(1L, 2L, 9, "Incredible cinema!", requester) } returns saved

        val response = controller.insert(1L, request, requester)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe ReviewResponse(saved)
    }

    @Test
    fun `insert should pass explicit userId to service when provided`() {
        val admin = oneAdminToken(id = 99L)
        val request = ReviewRequest(userId = 2L, rating = 8, comment = "Great visuals")
        val saved = oneReview(id = 11L, rating = 8, comment = "Great visuals", user = oneUser(id = 2L))
        every { service.insert(1L, 2L, 8, "Great visuals", admin) } returns saved

        val response = controller.insert(1L, request, admin)

        response.statusCode shouldBe HttpStatus.CREATED
        response.body shouldBe ReviewResponse(saved)
    }

    @Test
    fun `patch should return 200 OK when review was modified`() {
        val requester = oneUserToken(id = 2L)
        val request = PatchReviewRequest(rating = 10, comment = "Even better on rewatch")
        val updated = oneReview(id = 1L, rating = 10, comment = "Even better on rewatch")
        every { service.patch(1L, 10, "Even better on rewatch", requester) } returns updated

        val response = controller.patch(1L, request, requester)

        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe ReviewResponse(updated)
    }

    @Test
    fun `patch should return 204 NO_CONTENT when nothing changed`() {
        val requester = oneUserToken(id = 2L)
        val request = PatchReviewRequest(rating = 10)
        every { service.patch(1L, 10, null, requester) } returns null

        val response = controller.patch(1L, request, requester)

        response.statusCode shouldBe HttpStatus.NO_CONTENT
        response.body shouldBe null
    }

    @Test
    fun `deleteById should return 200 OK when deleted by owner or ADMIN`() {
        val requester = oneUserToken(id = 2L)
        every { service.deleteById(1L, requester) } returns true

        val response = controller.deleteById(1L, requester)

        response.statusCode shouldBe HttpStatus.OK
    }

    @Test
    fun `deleteById should return 404 NOT_FOUND when review does not exist`() {
        val requester = oneUserToken(id = 2L)
        every { service.deleteById(99L, requester) } returns false

        val response = controller.deleteById(99L, requester)

        response.statusCode shouldBe HttpStatus.NOT_FOUND
    }

    @Test
    fun `deleteById should propagate AccessDeniedException when requester is not owner nor ADMIN`() {
        val otherUser = oneUserToken(id = 3L)
        every { service.deleteById(1L, otherUser) } throws AccessDeniedException("Forbidden")

        shouldThrow<AccessDeniedException> {
            controller.deleteById(1L, otherUser)
        }
    }
}
