package br.pucpr.cinelog.movies

import br.pucpr.cinelog.directors.Director
import br.pucpr.cinelog.genres.Genre
import jakarta.persistence.*

@Entity
@Table(name = "MovieTable")
class Movie(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, length = 1000)
    var synopsis: String,

    @Column(nullable = false)
    var releaseYear: Int,

    @Column(nullable = false)
    var durationMinutes: Int,

    @Column(nullable = true)
    var posterUrl: String? = null,

    @ManyToOne
    @JoinColumn(name = "idDirector")
    var director: Director? = null,

    @ManyToMany
    @JoinTable(
        name = "MovieGenre",
        joinColumns = [JoinColumn(name = "idMovie")],
        inverseJoinColumns = [JoinColumn(name = "idGenre")]
    )
    var genres: MutableSet<Genre> = mutableSetOf()
)
