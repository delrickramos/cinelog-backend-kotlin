package br.pucpr.cinelog.roles

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/roles")
@Validated
class RoleController(
    val service: RoleService
) {
    @GetMapping
    fun list(): ResponseEntity<List<RoleResponse>> =
        service.findAll()
            .map { RoleResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun insert(
        @Valid @RequestBody role: RoleRequest
    ): ResponseEntity<RoleResponse> =
        service.insert(role.toRole())
            .let { RoleResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
}
