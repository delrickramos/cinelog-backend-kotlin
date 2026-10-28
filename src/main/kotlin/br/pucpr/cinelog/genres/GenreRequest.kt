package br.pucpr.cinelog.genres

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class GenreRequest(
    @NotBlank
    @Size(min = 2, max = 50)
    val name: String?,

    @NotBlank
    @Size(max = 255)
    val description: String?
) {
    fun toGenre() = Genre(
        id = null,
        name = name!!.trim(),
        description = description!!.trim()
    )
}
