package br.pucpr.cinelog.users.requests

import jakarta.validation.constraints.NotBlank

data class PatchUserRequest(
    @NotBlank
    var name: String?
)
