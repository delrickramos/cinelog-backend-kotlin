package br.pucpr.cinelog.directors

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class DirectorService(
    val repository: DirectorRepository
) {
    fun insert(director: Director): Director {
        if (repository.findByNameIgnoreCase(director.name) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Director '${director.name}' already exists")
        }
        return repository.save(director)
            .also { log.info("Director {} created with id={}", it.name, it.id) }
    }

    fun findAll(): List<Director> = repository.findAll(Sort.by("name"))

    fun findByIdOrNull(id: Long): Director? = repository.findByIdOrNull(id)

    companion object {
        private val log = LoggerFactory.getLogger(DirectorService::class.java)
    }
}
