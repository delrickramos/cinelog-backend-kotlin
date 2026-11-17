package br.pucpr.cinelog.movies.requests

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class PatchMovieRequest(
    @Size(min = 1, max = 150)
    val title: String? = null,

    @Size(min = 10, max = 1000)
    val synopsis: String? = null,

    @Min(1888)
    @Max(2030)
    val releaseYear: Int? = null,

    @Min(1)
    @Max(1000)
    val durationMinutes: Int? = null,

    val posterUrl: String? = null
)
