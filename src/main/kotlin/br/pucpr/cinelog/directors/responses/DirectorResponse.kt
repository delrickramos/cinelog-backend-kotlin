package br.pucpr.cinelog.directors.responses

import br.pucpr.cinelog.directors.Director

data class DirectorResponse(
    val id: Long,
    val name: String,
    val nationality: String,
    val birthYear: Int,
    val biography: String
) {
    constructor(director: Director) : this(
        id = director.id!!,
        name = director.name,
        nationality = director.nationality,
        birthYear = director.birthYear,
        biography = director.biography
    )
}
