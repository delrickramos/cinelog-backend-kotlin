package br.pucpr.cinelog.reviews

import br.pucpr.cinelog.movies.MovieRepository
import br.pucpr.cinelog.reviews.responses.ReviewStatsResponse
import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import kotlin.math.round

@Service
class ReviewService(
    val repository: ReviewRepository,
    val movieRepository: MovieRepository,
    val userRepository: UserRepository
) {
    fun insert(movieId: Long, userId: Long, rating: Int, comment: String): Review {
        val movie = movieRepository.findByIdOrNull(movieId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        val user = userRepository.findByIdOrNull(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User $userId not found!")

        val review = Review(
            rating = rating,
            comment = comment.trim(),
            user = user,
            movie = movie
        )
        return repository.save(review)
            .also { log.info("Review {} created by user={} for movie={}", it.id, userId, movieId) }
    }

    fun findAll(
        movieId: Long? = null,
        userId: Long? = null,
        minRating: Int? = null,
        dir: SortDir = SortDir.DESC
    ): List<Review> {
        val base = when {
            movieId != null -> repository.findByMovieId(movieId)
            userId != null -> repository.findByUserId(userId)
            else -> repository.findAll()
        }

        val filtered = base.asSequence()
            .filter { userId == null || it.user.id == userId }
            .filter { minRating == null || it.rating >= minRating }
            .toList()

        return if (dir == SortDir.ASC) {
            filtered.sortedBy { it.rating }
        } else {
            filtered.sortedByDescending { it.rating }
        }
    }

    fun findByIdOrNull(id: Long): Review? = repository.findByIdOrNull(id)

    fun getMovieStats(movieId: Long): ReviewStatsResponse {
        if (movieRepository.findByIdOrNull(movieId) == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Movie $movieId not found!")
        }
        val reviews = repository.findByMovieId(movieId)
        val count = reviews.size
        val avg = if (count == 0) 0.0 else round(reviews.map { it.rating }.average() * 10.0) / 10.0
        return ReviewStatsResponse(movieId = movieId, count = count, averageRating = avg)
    }

    fun patch(id: Long, rating: Int?, comment: String?): Review? {
        val review = repository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Review $id not found!")
        var changed = false
        if (rating != null && rating != review.rating) {
            review.rating = rating
            changed = true
        }
        if (!comment.isNullOrBlank() && comment.trim() != review.comment) {
            review.comment = comment.trim()
            changed = true
        }
        if (!changed) return null
        return repository.save(review)
            .also { log.info("Review {} updated", id) }
    }

    fun deleteById(id: Long): Boolean {
        val review = repository.findByIdOrNull(id) ?: return false
        repository.delete(review)
        log.warn("Review {} deleted", id)
        return true
    }

    companion object {
        private val log = LoggerFactory.getLogger(ReviewService::class.java)
    }
}
