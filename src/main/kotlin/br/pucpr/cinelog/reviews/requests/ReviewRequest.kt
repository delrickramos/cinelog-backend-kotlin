package br.pucpr.cinelog.reviews.requests

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReviewRequest(
    @Min(1)
    val userId: Long? = null,

    @NotNull
    @Min(0)
    @Max(10)
    val rating: Int?,

    @NotBlank
    @Size(min = 2, max = 1000)
    val comment: String?
)
