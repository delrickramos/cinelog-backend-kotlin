package br.pucpr.cinelog.reviews.requests

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class PatchReviewRequest(
    @Min(0)
    @Max(10)
    val rating: Int? = null,

    @Size(min = 2, max = 1000)
    val comment: String? = null
)
