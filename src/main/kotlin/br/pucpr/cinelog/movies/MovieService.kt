package br.pucpr.cinelog.movies

import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.users.SortDir
import org.slf4j.LoggerFactory
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

    fun findAll(
        genre: String? = null,
        directorId: Long? = null,
        minYear: Int? = null,
        title: String? = null,
        dir: SortDir = SortDir.ASC
    ): List<Movie> {
        val base = when {
            !genre.isNullOrBlank() -> repository.findByGenre(genre.trim())
            directorId != null -> repository.findByDirectorId(directorId)
            else -> repository.findAll()
        }

        val filtered = base.asSequence()
            .filter { directorId == null || it.director?.id == directorId }
            .filter { minYear == null || it.releaseYear >= minYear }
            .filter { title.isNullOrBlank() || it.title.contains(title.trim(), ignoreCase = true) }
            .toList()

        return if (dir == SortDir.ASC) {
            filtered.sortedBy { it.title.lowercase() }
        } else {
            filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    fun findByIdOrNull(id: Long): Movie? = repository.findByIdOrNull(id)

    fun update(id: Long, updated: Movie, directorId: Long? = null): Movie {
        val movie = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $id not found!")
        movie.title = updated.title
        movie.synopsis = updated.synopsis
        movie.releaseYear = updated.releaseYear
        movie.durationMinutes = updated.durationMinutes
        movie.posterUrl = updated.posterUrl
        if (directorId != null) {
            val director = directorRepository.findByIdOrNull(directorId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Director $directorId not found!")
            movie.director = director
        }
        return repository.save(movie)
            .also { log.info("Movie {} updated (PUT)", id) }
    }

    fun patch(
        id: Long,
        title: String?,
        synopsis: String?,
        releaseYear: Int?,
        durationMinutes: Int?,
        posterUrl: String?
    ): Movie? {
        val movie = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $id not found!")
        var changed = false
        if (!title.isNullOrBlank() && title.trim() != movie.title) {
            movie.title = title.trim()
            changed = true
        }
        if (!synopsis.isNullOrBlank() && synopsis.trim() != movie.synopsis) {
            movie.synopsis = synopsis.trim()
            changed = true
        }
        if (releaseYear != null && releaseYear != movie.releaseYear) {
            movie.releaseYear = releaseYear
            changed = true
        }
        if (durationMinutes != null && durationMinutes != movie.durationMinutes) {
            movie.durationMinutes = durationMinutes
            changed = true
        }
        if (posterUrl != null && posterUrl != movie.posterUrl) {
            movie.posterUrl = posterUrl
            changed = true
        }
        if (!changed) return null
        return repository.save(movie)
            .also { log.info("Movie {} patched", id) }
    }

    fun deleteById(id: Long): Boolean {
        val movie = repository.findByIdOrNull(id) ?: return false
        repository.delete(movie)
        log.warn("Movie {} ('{}') deleted", id, movie.title)
        return true
    }

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

    fun removeGenre(movieId: Long, genreName: String): Boolean {
        val movie = repository.findByIdOrNull(movieId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        val target = movie.genres.find { it.name.equals(genreName.trim(), ignoreCase = true) }
            ?: return false

        movie.genres.remove(target)
        repository.save(movie)
        log.warn("Genre '{}' removed from movie {}", target.name, movieId)
        return true
    }

    fun setDirector(movieId: Long, directorId: Long): Boolean {
        val movie = repository.findByIdOrNull(movieId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        val director = directorRepository.findByIdOrNull(directorId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Director $directorId not found!")
        if (movie.director?.id == director.id) return false

        movie.director = director
        repository.save(movie)
        log.info("Director {} associated with movie {}", directorId, movieId)
        return true
    }

    fun removeDirector(movieId: Long): Boolean {
        val movie = repository.findByIdOrNull(movieId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        if (movie.director == null) return false

        movie.director = null
        repository.save(movie)
        log.warn("Director disassociated from movie {}", movieId)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(MovieService::class.java)
    }
}
