package br.pucpr.cinelog.directors.requests

import br.pucpr.cinelog.directors.Director
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class DirectorRequest(
    @NotBlank
    @Size(min = 2, max = 120)
    val name: String?,

    @NotBlank
    @Size(min = 2, max = 80)
    val nationality: String?,

    @NotNull
    @Min(1880)
    @Max(2026)
    val birthYear: Int?,

    @NotBlank
    @Size(max = 1000)
    val biography: String?
) {
    fun toDirector() = Director(
        id = null,
        name = name!!.trim(),
        nationality = nationality!!.trim(),
        birthYear = birthYear!!,
        biography = biography!!.trim()
    )
}
