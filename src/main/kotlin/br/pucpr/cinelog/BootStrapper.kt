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
import br.pucpr.cinelog.users.UserProfile
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
        val premiumRole = roleRepository.findByName("PREMIUM") ?: roleRepository.save(
            Role(
                name = "PREMIUM",
                description = "Premium CineLog Reviewer"
            )
        )

        val admin = if (userRepository.findByRole("ADMIN").isEmpty()) {
            userRepository.save(
                User(
                    name = "Administrator",
                    password = "admin",
                    email = "admin@cinelog.com",
                    roles = mutableSetOf(adminRole),
                    profile = UserProfile(
                        bio = "Curador oficial do catálogo CineLog",
                        avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=admin",
                        favoriteMovieQuote = "May the Force be with you."
                    )
                )
            )
        } else {
            userRepository.findByRole("ADMIN").first()
        }

        val reviewer = userRepository.findByEmail("delrick@cinelog.com") ?: userRepository.save(
            User(
                name = "Delrick Ramos",
                password = "1234",
                email = "delrick@cinelog.com",
                roles = mutableSetOf(premiumRole),
                profile = UserProfile(
                    bio = "Cinéfilo e fã de ficção científica",
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=delrick",
                    favoriteMovieQuote = "Do not go gentle into that good night."
                )
            )
        )

        if (genreRepository.count() == 0L) {
            val sciFi = genreRepository.save(Genre(name = "Sci-Fi", description = "Ficção Científica e exploração espacial"))
            val drama = genreRepository.save(Genre(name = "Drama", description = "Narrativas dramáticas e desenvolvimento de personagens"))
            val adventure = genreRepository.save(Genre(name = "Adventure", description = "Jornadas épicas e aventura"))
            val animation = genreRepository.save(Genre(name = "Animation", description = "Filmes de animação tradicionais e digitais"))

            val nolan = directorRepository.save(
                Director(
                    name = "Christopher Nolan",
                    nationality = "British-American",
                    birthYear = 1970,
                    biography = "Diretor conhecido por narrativas não-lineares como Inception, Interstellar e Oppenheimer."
                )
            )
            val villeneuve = directorRepository.save(
                Director(
                    name = "Denis Villeneuve",
                    nationality = "Canadian",
                    birthYear = 1967,
                    biography = "Cineasta franco-canadense aclamado por Arrival, Blade Runner 2049 e a franquia Dune."
                )
            )
            val miyazaki = directorRepository.save(
                Director(
                    name = "Hayao Miyazaki",
                    nationality = "Japanese",
                    birthYear = 1941,
                    biography = "Co-fundador do Studio Ghibli e diretor de A Viagem de Chihiro e Meu Amigo Totoro."
                )
            )

            val interstellar = movieRepository.save(
                Movie(
                    title = "Interstellar",
                    synopsis = "Uma equipe de exploradores viaja através de um buraco de minhoca no espaço na tentativa de garantir a sobrevivência da humanidade.",
                    releaseYear = 2014,
                    durationMinutes = 169,
                    posterUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                    director = nolan,
                    genres = mutableSetOf(sciFi, drama, adventure)
                )
            )

            val dune2 = movieRepository.save(
                Movie(
                    title = "Dune: Part Two",
                    synopsis = "Paul Atreides se une a Chani e aos Fremen em uma jornada de vingança contra os conspiradores que destruíram sua família.",
                    releaseYear = 2024,
                    durationMinutes = 166,
                    posterUrl = "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                    director = villeneuve,
                    genres = mutableSetOf(sciFi, adventure)
                )
            )

            movieRepository.save(
                Movie(
                    title = "Spirited Away",
                    synopsis = "Durante a mudança de sua família para o subúrbio, uma menina de 10 anos entra em um mundo governado por deuses, bruxas e espíritos.",
                    releaseYear = 2001,
                    durationMinutes = 125,
                    posterUrl = "https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg",
                    director = miyazaki,
                    genres = mutableSetOf(animation, adventure)
                )
            )

            reviewRepository.save(
                Review(
                    rating = 10,
                    comment = "Obra-prima absoluta! A trilha sonora do Hans Zimmer e a física do Gargantua são inesquecíveis.",
                    user = reviewer,
                    movie = interstellar
                )
            )
            reviewRepository.save(
                Review(
                    rating = 9,
                    comment = "Fotografia impecável e escala épica. Denis Villeneuve entregou uma das melhores sequências da história.",
                    user = admin,
                    movie = dune2
                )
            )
        }
    }
}
