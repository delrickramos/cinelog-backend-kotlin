package br.pucpr.cinelog.genres

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenreRepository : JpaRepository<Genre, Long> {
    fun findByNameIgnoreCase(name: String): Genre?
}
