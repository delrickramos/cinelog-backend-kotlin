package br.pucpr.cinelog.genres

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PatchGenreRequest(
    @NotBlank
    @Size(max = 255)
    val description: String?
)
