package br.pucpr.cinelog.genres

import br.pucpr.cinelog.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class GenreService(
    val repository: GenreRepository
) {
    fun insert(genre: Genre): Genre {
        if (repository.findByNameIgnoreCase(genre.name) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Genre '${genre.name}' already exists")
        }
        return repository.save(genre)
            .also { log.info("Genre {} created with id={}", it.name, it.id) }
    }

    fun findAll(dir: SortDir = SortDir.ASC): List<Genre> =
        if (dir == SortDir.ASC)
            repository.findAll(Sort.by("name"))
        else
            repository.findAll(Sort.by("name").descending())

    fun findByIdOrNull(id: Long): Genre? = repository.findByIdOrNull(id)

    fun update(id: Long, description: String): Genre? {
        val genre = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Genre $id not found!")
        if (genre.description == description) return null
        genre.description = description
        return repository.save(genre)
            .also { log.info("Genre {} updated", id) }
    }

    fun deleteById(id: Long): Boolean {
        val genre = repository.findByIdOrNull(id) ?: return false
        repository.delete(genre)
        log.warn("Genre {} ({}) deleted", id, genre.name)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(GenreService::class.java)
    }
}
