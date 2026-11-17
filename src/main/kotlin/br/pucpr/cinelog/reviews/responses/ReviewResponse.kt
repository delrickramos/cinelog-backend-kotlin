package br.pucpr.cinelog.reviews.responses

import br.pucpr.cinelog.reviews.Review
import java.time.LocalDateTime

data class ReviewResponse(
    val id: Long,
    val movieId: Long,
    val movieTitle: String,
    val userId: Long,
    val userName: String,
    val rating: Int,
    val comment: String,
    val createdAt: LocalDateTime
) {
    constructor(review: Review) : this(
        id = review.id!!,
        movieId = review.movie.id!!,
        movieTitle = review.movie.title,
        userId = review.user.id!!,
        userName = review.user.name,
        rating = review.rating,
        comment = review.comment,
        createdAt = review.createdAt
    )
}
