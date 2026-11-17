package br.pucpr.cinelog.reviews.responses

data class ReviewStatsResponse(
    val movieId: Long,
    val count: Int,
    val averageRating: Double
)
