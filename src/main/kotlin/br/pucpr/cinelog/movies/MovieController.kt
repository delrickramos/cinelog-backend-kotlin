package br.pucpr.cinelog.movies

import br.pucpr.cinelog.movies.requests.MovieRequest
import br.pucpr.cinelog.movies.requests.PatchMovieRequest
import br.pucpr.cinelog.movies.responses.MovieResponse
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
@RequestMapping("/movies")
@Validated
class MovieController(
    private val service: MovieService
) {
    @GetMapping
    fun list(
        @RequestParam genre: String?,
        @RequestParam directorId: Long?,
        @RequestParam minYear: Int?,
        @RequestParam title: String?,
        @RequestParam sortDir: String?
    ): ResponseEntity<List<MovieResponse>> =
        SortDir.findOrThrow(sortDir ?: "ASC")
            .let { service.findAll(genre, directorId, minYear, title, it) }
            .map { MovieResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<MovieResponse> =
        service.findByIdOrNull(id)
            ?.let { MovieResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun insert(
        @Valid @RequestBody request: MovieRequest
    ): ResponseEntity<MovieResponse> =
        service.insert(request.toMovie(), request.directorId)
            .let { MovieResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: MovieRequest
    ): ResponseEntity<MovieResponse> =
        service.update(id, request.toMovie(), request.directorId)
            .let { MovieResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun patch(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: PatchMovieRequest
    ): ResponseEntity<MovieResponse> =
        service.patch(
            id = id,
            title = request.title,
            synopsis = request.synopsis,
            releaseYear = request.releaseYear,
            durationMinutes = request.durationMinutes,
            posterUrl = request.posterUrl
        )
            ?.let { MovieResponse(it) }
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

    @PutMapping("/{id}/genres/{genreName}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun addGenre(
        @Min(1) @PathVariable id: Long,
        @PathVariable genreName: String
    ): ResponseEntity<Void> =
        if (service.addGenre(id, genreName)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @DeleteMapping("/{id}/genres/{genreName}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun removeGenre(
        @Min(1) @PathVariable id: Long,
        @PathVariable genreName: String
    ): ResponseEntity<Void> =
        if (service.removeGenre(id, genreName)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @PutMapping("/{id}/director/{directorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun setDirector(
        @Min(1) @PathVariable id: Long,
        @Min(1) @PathVariable directorId: Long
    ): ResponseEntity<Void> =
        if (service.setDirector(id, directorId)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()

    @DeleteMapping("/{id}/director")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "AuthServer")
    fun removeDirector(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<Void> =
        if (service.removeDirector(id)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()
}
