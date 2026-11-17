package br.pucpr.cinelog.directors.requests

import jakarta.validation.constraints.Size

data class PatchDirectorRequest(
    @Size(min = 2, max = 120)
    val name: String? = null,

    @Size(min = 2, max = 80)
    val nationality: String? = null,

    @Size(max = 1000)
    val biography: String? = null
)
