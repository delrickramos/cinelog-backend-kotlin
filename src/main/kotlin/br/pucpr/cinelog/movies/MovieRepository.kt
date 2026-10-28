package br.pucpr.cinelog.movies

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface MovieRepository : JpaRepository<Movie, Long> {
    fun findByTitleIgnoreCase(title: String): Movie?

    @Query(
        """
            select distinct m from Movie m
            join m.genres g
            where upper(g.name) = upper(:genre)
        """
    )
    fun findByGenre(genre: String): List<Movie>

    fun findByDirectorId(directorId: Long): List<Movie>
}
