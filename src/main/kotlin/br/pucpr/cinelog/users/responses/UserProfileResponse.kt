package br.pucpr.cinelog.users.responses

import br.pucpr.cinelog.users.UserProfile

data class UserProfileResponse(
    val id: Long,
    val bio: String,
    val avatarUrl: String?,
    val favoriteMovieQuote: String?
) {
    constructor(profile: UserProfile) : this(
        id = profile.id!!,
        bio = profile.bio,
        avatarUrl = profile.avatarUrl,
        favoriteMovieQuote = profile.favoriteMovieQuote
    )
}
