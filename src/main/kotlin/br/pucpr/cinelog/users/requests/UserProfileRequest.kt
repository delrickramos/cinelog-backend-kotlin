package br.pucpr.cinelog.users.requests

import br.pucpr.cinelog.users.UserProfile
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserProfileRequest(
    @NotBlank
    @Size(max = 500)
    var bio: String?,

    var avatarUrl: String? = null,

    @Size(max = 255)
    var favoriteMovieQuote: String? = null
) {
    fun toUserProfile() = UserProfile(
        id = null,
        bio = bio!!,
        avatarUrl = avatarUrl,
        favoriteMovieQuote = favoriteMovieQuote
    )
}
