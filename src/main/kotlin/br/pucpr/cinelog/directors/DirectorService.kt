package br.pucpr.cinelog.directors

import br.pucpr.cinelog.users.SortDir
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

    fun findAll(nationality: String? = null, dir: SortDir = SortDir.ASC): List<Director> {
        val sort = if (dir == SortDir.ASC) Sort.by("name") else Sort.by("name").descending()
        return if (!nationality.isNullOrBlank()) {
            repository.findByNationalityIgnoreCase(nationality.trim(), sort)
        } else {
            repository.findAll(sort)
        }
    }

    fun findByIdOrNull(id: Long): Director? = repository.findByIdOrNull(id)

    fun update(id: Long, updated: Director): Director {
        val director = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Director $id not found!")
        director.name = updated.name
        director.nationality = updated.nationality
        director.birthYear = updated.birthYear
        director.biography = updated.biography
        return repository.save(director)
            .also { log.info("Director {} updated (PUT)", id) }
    }

    fun patch(id: Long, name: String?, nationality: String?, biography: String?): Director? {
        val director = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Director $id not found!")
        var changed = false
        if (!name.isNullOrBlank() && name.trim() != director.name) {
            director.name = name.trim()
            changed = true
        }
        if (!nationality.isNullOrBlank() && nationality.trim() != director.nationality) {
            director.nationality = nationality.trim()
            changed = true
        }
        if (!biography.isNullOrBlank() && biography.trim() != director.biography) {
            director.biography = biography.trim()
            changed = true
        }
        if (!changed) return null
        return repository.save(director)
            .also { log.info("Director {} patched", id) }
    }

    fun deleteById(id: Long): Boolean {
        val director = repository.findByIdOrNull(id) ?: return false
        repository.delete(director)
        log.warn("Director {} ({}) deleted", id, director.name)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(DirectorService::class.java)
    }
}
