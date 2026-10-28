package br.pucpr.cinelog.directors

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DirectorRepository : JpaRepository<Director, Long> {
    fun findByNameIgnoreCase(name: String): Director?
    fun findByNationalityIgnoreCase(nationality: String, sort: Sort): List<Director>
}
