package br.pucpr.cinelog.movies.requests

import br.pucpr.cinelog.movies.Movie
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class MovieRequest(
    @NotBlank
    @Size(min = 1, max = 150)
    val title: String?,

    @NotBlank
    @Size(min = 10, max = 1000)
    val synopsis: String?,

    @NotNull
    @Min(1888)
    @Max(2030)
    val releaseYear: Int?,

    @NotNull
    @Min(1)
    @Max(1000)
    val durationMinutes: Int?,

    val posterUrl: String? = null,

    val directorId: Long? = null
) {
    fun toMovie() = Movie(
        id = null,
        title = title!!.trim(),
        synopsis = synopsis!!.trim(),
        releaseYear = releaseYear!!,
        durationMinutes = durationMinutes!!,
        posterUrl = posterUrl
    )
}
