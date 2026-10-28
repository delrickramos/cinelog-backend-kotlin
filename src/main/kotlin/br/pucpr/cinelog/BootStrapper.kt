package br.pucpr.cinelog

import br.pucpr.cinelog.directors.Director
import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.Genre
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.movies.Movie
import br.pucpr.cinelog.movies.MovieRepository
import br.pucpr.cinelog.reviews.Review
import br.pucpr.cinelog.reviews.ReviewRepository
import br.pucpr.cinelog.roles.Role
import br.pucpr.cinelog.roles.RoleRepository
import br.pucpr.cinelog.users.User
import br.pucpr.cinelog.users.UserRepository
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class BootStrapper(
    val roleRepository: RoleRepository,
    val userRepository: UserRepository,
    val genreRepository: GenreRepository,
    val directorRepository: DirectorRepository,
    val movieRepository: MovieRepository,
    val reviewRepository: ReviewRepository
) : ApplicationListener<ContextRefreshedEvent> {

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val adminRole = roleRepository.findByName("ADMIN") ?: roleRepository
            .save(Role(name = "ADMIN", description = "System Administrator"))
        roleRepository.findByName("PREMIUM") ?: roleRepository.save(
            Role(
                name = "PREMIUM",
                description = "Premium User"
            )
        )
        val admin = if (userRepository.findByRole("ADMIN").isEmpty()) {
            val user = User(
                name = "Administrator",
                password = "admin",
                email = "admin@cinelog.com"
            )
            user.roles.add(adminRole)
            userRepository.save(user)
        } else {
            userRepository.findByRole("ADMIN").first()
        }

        if (genreRepository.count() == 0L) {
            val sciFi = genreRepository.save(Genre(name = "Sci-Fi", description = "Ficção Científica"))
            val drama = genreRepository.save(Genre(name = "Drama", description = "Filmes de Drama"))

            val nolan = directorRepository.save(
                Director(
                    name = "Christopher Nolan",
                    nationality = "British-American",
                    birthYear = 1970,
                    biography = "Diretor de Inception, Interstellar e Oppenheimer."
                )
            )

            val interstellar = movieRepository.save(
                Movie(
                    title = "Interstellar",
                    synopsis = "Uma equipe de exploradores viaja através de um buraco de minhoca no espaço.",
                    releaseYear = 2014,
                    durationMinutes = 169,
                    director = nolan,
                    genres = mutableSetOf(sciFi, drama)
                )
            )

            reviewRepository.save(
                Review(
                    rating = 10,
                    comment = "Obra-prima absoluta!",
                    user = admin,
                    movie = interstellar
                )
            )
        }
    }
}
