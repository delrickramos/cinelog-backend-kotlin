package br.pucpr.cinelog.movies

import br.pucpr.cinelog.movies.requests.MovieRequest
import br.pucpr.cinelog.movies.requests.PatchMovieRequest
import br.pucpr.cinelog.movies.responses.MovieResponse
import br.pucpr.cinelog.users.SortDir
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        @RequestParam sortDir: String?,
        @RequestParam genre: String?
    ): ResponseEntity<List<MovieResponse>> {
        val movies = if (genre != null) service.findByGenre(genre)
        else SortDir.findOrThrow(sortDir ?: "ASC")
            .let { service.findAll(it) }

        return movies
            .map { MovieResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    fun getById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<MovieResponse> =
        service.findByIdOrNull(id)
            ?.let { MovieResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    fun insert(
        @Valid @RequestBody request: MovieRequest
    ): ResponseEntity<MovieResponse> =
        service.insert(request.toMovie(), request.directorId)
            .let { MovieResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PatchMapping("/{id}")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody request: PatchMovieRequest
    ): ResponseEntity<MovieResponse> =
        service.patch(id, request.title)
            ?.let { MovieResponse(it) }
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.noContent().build()

    @DeleteMapping("/{id}")
    fun deleteById(
        @Min(1) @PathVariable id: Long
    ): ResponseEntity<Void> =
        service.deleteById(id)
            .let { ResponseEntity.ok().build() }

    @PutMapping("/{id}/genres/{genreName}")
    fun addGenre(
        @Min(1) @PathVariable id: Long,
        @PathVariable genreName: String
    ): ResponseEntity<Void> =
        if (service.addGenre(id, genreName)) ResponseEntity.ok().build()
        else ResponseEntity.noContent().build()
}
