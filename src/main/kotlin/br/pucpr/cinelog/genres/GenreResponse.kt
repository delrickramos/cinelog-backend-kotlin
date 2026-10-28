package br.pucpr.cinelog.genres

data class GenreResponse(
    val id: Long,
    val name: String,
    val description: String
) {
    constructor(genre: Genre) : this(
        id = genre.id!!,
        name = genre.name,
        description = genre.description
    )
}
