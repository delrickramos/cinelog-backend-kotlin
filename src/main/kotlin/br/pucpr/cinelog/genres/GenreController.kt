package br.pucpr.cinelog.genres

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/genres")
@Validated
class GenreController(
    val service: GenreService
) {
    @GetMapping
    fun list(): ResponseEntity<List<GenreResponse>> =
        service.findAll()
            .map { GenreResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun insert(
        @Valid @RequestBody genre: GenreRequest
    ): ResponseEntity<GenreResponse> =
        service.insert(genre.toGenre())
            .let { GenreResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
}
