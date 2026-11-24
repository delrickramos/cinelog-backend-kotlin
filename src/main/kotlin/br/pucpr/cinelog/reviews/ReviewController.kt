package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.reviews.requests.PatchReviewRequest
import br.pucpr.cinelog.reviews.requests.ReviewRequest
import br.pucpr.cinelog.reviews.responses.ReviewResponse
import br.pucpr.cinelog.reviews.responses.ReviewStatsResponse
import br.pucpr.cinelog.security.UserToken
import br.pucpr.cinelog.users.SortDir
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@Validated
class ReviewController(
    private val service: ReviewService
) {
    @GetMapping("/reviews")
    fun list(
        @RequestParam movieId: Long?,
        @RequestParam userId: Long?,
        @RequestParam minRating: Int?,
        @RequestParam sortDir: String?
    ): ResponseEntity<List<ReviewResponse>> =
        SortDir.findOrThrow(sortDir ?: "DESC")
            .let { service.findAll(movieId, userId, minRating, it) }
            .map { ReviewResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/reviews/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<ReviewResponse> =
        service.findByIdOrNull(id)
            ?.let { ReviewResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/movies/{movieId}/reviews")
    fun listByMovie(
        @Min(1) @PathVariable movieId: Long,
        @RequestParam sortDir: String?
    ): ResponseEntity<List<ReviewResponse>> =
        SortDir.findOrThrow(sortDir ?: "DESC")
            .let { service.findAll(movieId = movieId, dir = it) }
            .map { ReviewResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/movies/{movieId}/reviews/stats")
    fun statsByMovie(
        @Min(1) @PathVariable movieId: Long
    ): ResponseEntity<ReviewStatsResponse> =
        service.getMovieStats(movieId)
            .let { ResponseEntity.ok(it) }

    @PostMapping("/movies/{movieId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "AuthServer")
    fun insert(
        @Min(1) @PathVariable movieId: Long,
        @Valid @RequestBody request: ReviewRequest,
        @AuthenticationPrincipal requester: UserToken
    ): ResponseEntity<ReviewResponse> =
        service.insert(movieId, request.userId ?: requester.id, request.rating!!, request.comment!!, requester)
            .let { ReviewResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PatchMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "AuthServer")
    fun patch(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: PatchReviewRequest,
        @AuthenticationPrincipal requester: UserToken
    ): ResponseEntity<ReviewResponse> =
        service.patch(id, request.rating, request.comment, requester)
            ?.let { ReviewResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "AuthServer")
    fun deleteById(
        @Min(1) @PathVariable id: Long,
        @AuthenticationPrincipal requester: UserToken
    ): ResponseEntity<Void> =
        if (service.deleteById(id, requester)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()
}
