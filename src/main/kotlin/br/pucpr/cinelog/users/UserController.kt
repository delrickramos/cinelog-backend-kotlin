package br.pucpr.cinelog.users

import br.pucpr.cinelog.security.UserToken
import br.pucpr.cinelog.users.requests.LoginRequest
import br.pucpr.cinelog.users.requests.PatchUserRequest
import br.pucpr.cinelog.users.requests.UserProfileRequest
import br.pucpr.cinelog.users.requests.UserRequest
import br.pucpr.cinelog.users.responses.LoginResponse
import br.pucpr.cinelog.users.responses.UserResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Validated
class UserController(
    private val userService: UserService
) {
    @GetMapping
    fun list(
        @RequestParam sortDir: String?,
        @RequestParam role: String?
    ): ResponseEntity<List<UserResponse>> {
        val users = if (role != null) userService.findByRole(role)
        else SortDir.findOrThrow(sortDir ?: "ASC")
            .let { userService.findAll(it) }

        return users
            .map { UserResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @PostMapping
    fun insert(
        @Valid @RequestBody user: UserRequest
    ): ResponseEntity<UserResponse> =
        userService.insert(user.toUser())
            .let { UserResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody login: LoginRequest
    ): ResponseEntity<LoginResponse> =
        userService.login(login.email!!, login.password!!)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "AuthServer")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody user: PatchUserRequest,
        @AuthenticationPrincipal requester: UserToken
    ): ResponseEntity<UserResponse> =
        userService.update(id, user.name!!, requester)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

    @PutMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "AuthServer")
    fun updateProfile(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody profile: UserProfileRequest,
        @AuthenticationPrincipal requester: UserToken
    ): ResponseEntity<UserResponse> =
        userService.updateProfile(id, profile.toUserProfile(), requester)
            .let { UserResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<UserResponse> =
        userService.findByIdOrNull(id)
            ?.let { UserResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun deleteById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<Void> =
        if (userService.deleteById(id)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()

    @PutMapping("/{id}/roles/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun grant(
        @Min(1) @PathVariable id: Long,
        @PathVariable name: String
    ): ResponseEntity<Void> =
        if (userService.addRole(id, name)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()
}
