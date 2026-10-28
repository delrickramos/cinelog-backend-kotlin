package br.pucpr.cinelog.movies.requests

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PatchMovieRequest(
    @NotBlank
    @Size(min = 1, max = 150)
    val title: String?
)
