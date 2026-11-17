package br.pucpr.cinelog.users

import jakarta.persistence.*

@Entity
@Table(name = "UserProfileTable")
class UserProfile(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false, length = 500)
    var bio: String = "",

    @Column(nullable = true)
    var avatarUrl: String? = null,

    @Column(nullable = true)
    var favoriteMovieQuote: String? = null
)
