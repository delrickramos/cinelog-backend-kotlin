package br.pucpr.cinelog.directors

import br.pucpr.cinelog.directors.requests.DirectorRequest
import br.pucpr.cinelog.directors.requests.PatchDirectorRequest
import br.pucpr.cinelog.directors.responses.DirectorResponse
import br.pucpr.cinelog.users.SortDir
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/directors")
@Validated
class DirectorController(
    private val service: DirectorService
) {
    @GetMapping
    fun list(
        @RequestParam nationality: String?,
        @RequestParam sortDir: String?
    ): ResponseEntity<List<DirectorResponse>> =
        SortDir.findOrThrow(sortDir ?: "ASC")
            .let { service.findAll(nationality, it) }
            .map { DirectorResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<DirectorResponse> =
        service.findByIdOrNull(id)
            ?.let { DirectorResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun insert(
        @Valid @RequestBody request: DirectorRequest
    ): ResponseEntity<DirectorResponse> =
        service.insert(request.toDirector())
            .let { DirectorResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: DirectorRequest
    ): ResponseEntity<DirectorResponse> =
        service.update(id, request.toDirector())
            .let { DirectorResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun patch(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: PatchDirectorRequest
    ): ResponseEntity<DirectorResponse> =
        service.patch(id, request.name, request.nationality, request.biography)
            ?.let { DirectorResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun deleteById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<Void> =
        if (service.deleteById(id)) ResponseEntity.ok().build()
        else ResponseEntity.notFound().build()
}
