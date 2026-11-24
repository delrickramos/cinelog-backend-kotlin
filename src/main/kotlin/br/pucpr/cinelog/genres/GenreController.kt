package br.pucpr.cinelog.genres

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
@RequestMapping("/genres")
@Validated
class GenreController(
    val service: GenreService
) {
    @GetMapping
    fun list(
        @RequestParam sortDir: String?
    ): ResponseEntity<List<GenreResponse>> =
        SortDir.findOrThrow(sortDir ?: "ASC")
            .let { service.findAll(it) }
            .map { GenreResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<GenreResponse> =
        service.findByIdOrNull(id)
            ?.let { GenreResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun insert(
        @Valid @RequestBody genre: GenreRequest
    ): ResponseEntity<GenreResponse> =
        service.insert(genre.toGenre())
            .let { GenreResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: PatchGenreRequest
    ): ResponseEntity<GenreResponse> =
        service.update(id, request.description!!)
            ?.let { GenreResponse(it) }
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
