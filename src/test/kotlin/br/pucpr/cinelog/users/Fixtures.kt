package br.pucpr.cinelog.users

import br.pucpr.cinelog.directors.Director
import br.pucpr.cinelog.genres.Genre
import br.pucpr.cinelog.movies.Movie
import br.pucpr.cinelog.reviews.Review
import br.pucpr.cinelog.roles.Role
import br.pucpr.cinelog.security.UserToken

fun oneRole(
    id: Long? = 1L,
    name: String = "PREMIUM",
    description: String = "Premium CineLog Reviewer"
) = Role(
    id = id,
    name = name,
    description = description
)

fun oneUserToken(
    id: Long = 1L,
    name: String = "John Doe",
    roles: Set<String> = setOf("PREMIUM")
) = UserToken(
    id = id,
    name = name,
    roles = roles
)

fun oneAdminToken(
    id: Long = 99L,
    name: String = "Administrator",
    roles: Set<String> = setOf("ADMIN")
) = UserToken(
    id = id,
    name = name,
    roles = roles
)

fun oneUser(
    id: Long? = 1L,
    name: String = "John Doe",
    email: String = "${name.lowercase().replace(" ", ".")}@email.com",
    password: String = "password",
    roles: MutableSet<Role> = mutableSetOf(),
    profile: UserProfile? = null
) = User(
    id = id,
    email = email,
    password = password,
    name = name,
    roles = roles,
    profile = profile
)

fun oneDirector(
    id: Long? = 1L,
    name: String = "Christopher Nolan",
    nationality: String = "British-American",
    birthYear: Int = 1970,
    biography: String = "Acclaimed filmmaker"
) = Director(
    id = id,
    name = name,
    nationality = nationality,
    birthYear = birthYear,
    biography = biography
)

fun oneGenre(
    id: Long? = 1L,
    name: String = "Sci-Fi",
    description: String = "Science Fiction"
) = Genre(
    id = id,
    name = name,
    description = description
)

fun oneMovie(
    id: Long? = 1L,
    title: String = "Interstellar",
    synopsis: String = "A team of explorers travel through a wormhole in space.",
    releaseYear: Int = 2014,
    durationMinutes: Int = 169,
    posterUrl: String? = "https://example.com/poster.jpg",
    director: Director? = oneDirector(),
    genres: MutableSet<Genre> = mutableSetOf(oneGenre())
) = Movie(
    id = id,
    title = title,
    synopsis = synopsis,
    releaseYear = releaseYear,
    durationMinutes = durationMinutes,
    posterUrl = posterUrl,
    director = director,
    genres = genres
)

fun oneReview(
    id: Long? = 1L,
    rating: Int = 10,
    comment: String = "Masterpiece!",
    user: User = oneUser(),
    movie: Movie = oneMovie()
) = Review(
    id = id,
    rating = rating,
    comment = comment,
    user = user,
    movie = movie
)
