package br.pucpr.cinelog.users

import br.pucpr.cinelog.roles.RoleRepository
import br.pucpr.cinelog.security.JWT
import br.pucpr.cinelog.security.UserToken
import br.pucpr.cinelog.users.responses.LoginResponse
import br.pucpr.cinelog.users.responses.UserResponse
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    val repository: UserRepository,
    val roleRepository: RoleRepository,
    val jwt: JWT
) {
    fun insert(user: User): User {
        if (repository.findByEmail(user.email) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")
        }
        return repository
            .save(user)
            .also { log.info("User {} created!", it.id) }
    }

    fun update(id: Long, name: String, requester: UserToken): User? {
        if (!requester.isAdmin && requester.id != id) {
            throw AccessDeniedException("Only the user or an ADMIN can update this user")
        }
        val user = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $id not found!")
        if (user.name == name) return null
        user.name = name
        return repository.save(user)
            .also { log.info("User {} updated by user {}!", it.id, requester.id) }
    }

    fun updateProfile(id: Long, profile: UserProfile, requester: UserToken): User {
        if (!requester.isAdmin && requester.id != id) {
            throw AccessDeniedException("Only the user or an ADMIN can update this profile")
        }
        val user = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $id not found!")
        val existing = user.profile
        if (existing != null) {
            existing.bio = profile.bio
            existing.avatarUrl = profile.avatarUrl
            existing.favoriteMovieQuote = profile.favoriteMovieQuote
        } else {
            user.profile = profile
        }
        return repository.save(user)
            .also { log.info("Profile updated for user {} by user {}", id, requester.id) }
    }

    fun findAll(dir: SortDir): List<User> =
        if (dir == SortDir.ASC)
            repository.findAll(Sort.by("name"))
        else
            repository.findAll(Sort.by("name").descending())

    fun findByRole(role: String): List<User> = repository.findByRole(role.uppercase())

    fun findByIdOrNull(id: Long): User? = repository.findByIdOrNull(id)

    fun deleteById(id: Long): Boolean {
        val user = repository.findByIdOrNull(id) ?: return false
        if (user.roles.any { it.name == "ADMIN" }) {
            val count = repository.findByRole("ADMIN").size
            if (count <= 1) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete the last system administrator!")
            }
        }
        repository.delete(user)
        log.warn("User {} deleted", id)
        return true
    }

    fun addRole(id: Long, roleName: String): Boolean {
        val upperRole = roleName.uppercase()
        val user = findByIdOrNull(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $id not found!")
        if (user.roles.any { it.name == upperRole }) return false
        val role = roleRepository.findByName(upperRole) ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Role $upperRole not found!"
        )

        user.roles.add(role)
        repository.save(user)
        log.info("Granted role {} to user {}", upperRole, id)
        return true
    }

    fun login(email: String, password: String): LoginResponse? {
        val user = repository.findByEmail(email) ?: return null
        if (user.password != password) return null
        log.info("User logged in. id={}, name={}", user.id, user.name)
        return LoginResponse(
            token = jwt.createToken(user),
            user = UserResponse(user)
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }
}
