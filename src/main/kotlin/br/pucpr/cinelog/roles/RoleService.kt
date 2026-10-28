package br.pucpr.cinelog.roles

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class RoleService(
    val repository: RoleRepository
) {
    fun insert(role: Role): Role {
        role.name = role.name.uppercase()
        if (repository.findByName(role.name) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Role ${role.name} already exists")
        }
        return repository.save(role)
            .also { log.info("Role {} created!", it.name) }
    }

    fun findAll(): List<Role> = repository.findAll(Sort.by("name"))

    companion object {
        private val log = LoggerFactory.getLogger(RoleService::class.java)
    }
}
