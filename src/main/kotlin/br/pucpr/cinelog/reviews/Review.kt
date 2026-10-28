package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.movies.Movie
import br.pucpr.cinelog.users.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ReviewTable")
class Review(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var rating: Int,

    @Column(nullable = false, length = 1000)
    var comment: String,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(optional = false)
    @JoinColumn(name = "idUser", nullable = false)
    var user: User,

    @ManyToOne(optional = false)
    @JoinColumn(name = "idMovie", nullable = false)
    var movie: Movie
)
