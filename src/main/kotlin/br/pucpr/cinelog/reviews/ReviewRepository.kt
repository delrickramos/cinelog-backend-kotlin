package br.pucpr.cinelog.reviews

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByMovieId(movieId: Long): List<Review>
    fun findByUserId(userId: Long): List<Review>
}
