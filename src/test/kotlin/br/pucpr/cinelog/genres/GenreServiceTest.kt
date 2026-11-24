package br.pucpr.cinelog.genres

import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneGenre
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.checkUnnecessaryStub
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class GenreServiceTest {
    private val repository = mockk<GenreRepository>()
    private val service = GenreService(repository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository)
    }

    @Test
    fun `insert should save and return genre when name is unique`() {
        val genre = oneGenre(id = null)
        val saved = oneGenre(id = 1L)
        every { repository.findByNameIgnoreCase(genre.name) } returns null
        every { repository.save(genre) } returns saved

        service.insert(genre) shouldBe saved
    }

    @Test
    fun `insert should throw CONFLICT when genre name already exists`() {
        val genre = oneGenre(id = null)
        every { repository.findByNameIgnoreCase(genre.name) } returns oneGenre(id = 1L)

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(genre)
        }
        ex.statusCode shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `findAll should sort by name according to SortDir`() {
        val g1 = oneGenre(id = 1L, name = "Sci-Fi")
        val g2 = oneGenre(id = 2L, name = "Drama")
        every { repository.findAll(Sort.by("name").descending()) } returns listOf(g1, g2)

        service.findAll(SortDir.DESC) shouldBe listOf(g1, g2)
    }

    @Test
    fun `update should update description when different`() {
        val genre = oneGenre(id = 1L, description = "Old desc")
        every { repository.findByIdOrNull(1L) } returns genre
        every { repository.save(genre) } returns genre

        val result = service.update(1L, "New desc")

        result?.description shouldBe "New desc"
    }

    @Test
    fun `update should return null when description is unchanged`() {
        val genre = oneGenre(id = 1L, description = "Same desc")
        every { repository.findByIdOrNull(1L) } returns genre

        service.update(1L, "Same desc") shouldBe null
    }

    @Test
    fun `deleteById should delete genre and return true`() {
        val genre = oneGenre(id = 1L)
        every { repository.findByIdOrNull(1L) } returns genre
        justRun { repository.delete(genre) }

        service.deleteById(1L) shouldBe true
        verify(exactly = 1) { repository.delete(genre) }
    }

    @Test
    fun `deleteById should return false when genre is not found`() {
        every { repository.findByIdOrNull(99L) } returns null

        service.deleteById(99L) shouldBe false
    }
}
