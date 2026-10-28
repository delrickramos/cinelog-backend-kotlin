package br.pucpr.cinelog.genres

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
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

    fun findAll(): List<Genre> = repository.findAll(Sort.by("name"))

    companion object {
        private val log = LoggerFactory.getLogger(GenreService::class.java)
    }
}
