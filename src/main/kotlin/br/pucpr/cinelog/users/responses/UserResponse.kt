package br.pucpr.cinelog.users.responses

import br.pucpr.cinelog.users.User

data class UserResponse(
    var id: Long,
    var email: String,
    var name: String,
    var roles: Set<String> = emptySet(),
    var profile: UserProfileResponse? = null
) {
    constructor(user: User) : this(
        id = user.id!!,
        email = user.email,
        name = user.name,
        roles = user.roles.map { it.name }.toSortedSet(),
        profile = user.profile?.let { UserProfileResponse(it) }
    )
}
