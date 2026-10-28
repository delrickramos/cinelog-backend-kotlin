package br.pucpr.cinelog.movies

import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class MovieService(
    val repository: MovieRepository,
    val directorRepository: DirectorRepository,
    val genreRepository: GenreRepository
) {
    fun insert(movie: Movie, directorId: Long? = null): Movie {
        if (repository.findByTitleIgnoreCase(movie.title) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Movie '${movie.title}' already exists")
        }
        if (directorId != null) {
            val director = directorRepository.findByIdOrNull(directorId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Director $directorId not found!")
            movie.director = director
        }
        return repository.save(movie)
            .also { log.info("Movie '{}' created with id={}", it.title, it.id) }
    }

    fun findAll(dir: SortDir = SortDir.ASC): List<Movie> =
        if (dir == SortDir.ASC)
            repository.findAll(Sort.by("title"))
        else
            repository.findAll(Sort.by("title").descending())

    fun findByGenre(genre: String): List<Movie> = repository.findByGenre(genre.trim())

    fun findByIdOrNull(id: Long): Movie? = repository.findByIdOrNull(id)

    fun patch(id: Long, title: String?): Movie? {
        val movie = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $id not found!")
        if (title.isNullOrBlank() || movie.title == title.trim()) return null
        movie.title = title.trim()
        return repository.save(movie)
            .also { log.info("Movie {} updated", id) }
    }

    fun deleteById(id: Long) = repository
        .deleteById(id)
        .also { log.warn("Movie {} deleted", id) }

    fun addGenre(movieId: Long, genreName: String): Boolean {
        val movie = repository.findByIdOrNull(movieId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        val genre = genreRepository.findByNameIgnoreCase(genreName.trim())
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Genre '$genreName' not found!")
        if (movie.genres.any { it.name.equals(genre.name, ignoreCase = true) }) return false

        movie.genres.add(genre)
        repository.save(movie)
        log.info("Genre '{}' added to movie {}", genre.name, movieId)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(MovieService::class.java)
    }
}
