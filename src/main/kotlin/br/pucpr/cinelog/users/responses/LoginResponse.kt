package br.pucpr.cinelog.users.responses

data class LoginResponse(
    val token: String,
    val user: UserResponse
)
