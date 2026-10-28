package br.pucpr.cinelog.directors

import br.pucpr.cinelog.directors.requests.DirectorRequest
import br.pucpr.cinelog.directors.responses.DirectorResponse
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/directors")
@Validated
class DirectorController(
    private val service: DirectorService
) {
    @GetMapping
    fun list(): ResponseEntity<List<DirectorResponse>> =
        service.findAll()
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
    fun insert(
        @Valid @RequestBody request: DirectorRequest
    ): ResponseEntity<DirectorResponse> =
        service.insert(request.toDirector())
            .let { DirectorResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
}
