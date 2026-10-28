package br.pucpr.cinelog.movies.responses

import br.pucpr.cinelog.directors.responses.DirectorResponse
import br.pucpr.cinelog.genres.GenreResponse
import br.pucpr.cinelog.movies.Movie

data class MovieResponse(
    val id: Long,
    val title: String,
    val synopsis: String,
    val releaseYear: Int,
    val durationMinutes: Int,
    val posterUrl: String?,
    val director: DirectorResponse?,
    val genres: List<GenreResponse>
) {
    constructor(movie: Movie) : this(
        id = movie.id!!,
        title = movie.title,
        synopsis = movie.synopsis,
        releaseYear = movie.releaseYear,
        durationMinutes = movie.durationMinutes,
        posterUrl = movie.posterUrl,
        director = movie.director?.let { DirectorResponse(it) },
        genres = movie.genres.map { GenreResponse(it) }.sortedBy { it.name }
    )
}
