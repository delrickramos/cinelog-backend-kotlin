package br.pucpr.cinelog.directors

import br.pucpr.cinelog.users.SortDir
import br.pucpr.cinelog.users.oneDirector
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

class DirectorServiceTest {
    private val repository = mockk<DirectorRepository>()
    private val service = DirectorService(repository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        checkUnnecessaryStub(repository)
    }

    @Test
    fun `insert should save and return director when name is unique`() {
        val director = oneDirector(id = null)
        val saved = oneDirector(id = 1L)
        every { repository.findByNameIgnoreCase(director.name) } returns null
        every { repository.save(director) } returns saved

        service.insert(director) shouldBe saved
    }

    @Test
    fun `insert should throw CONFLICT when director name already exists`() {
        val director = oneDirector(id = null)
        every { repository.findByNameIgnoreCase(director.name) } returns oneDirector(id = 1L)

        val ex = shouldThrow<ResponseStatusException> {
            service.insert(director)
        }
        ex.statusCode shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `findAll should filter by nationality when provided`() {
        val director = oneDirector(id = 1L, nationality = "Canadian")
        every {
            repository.findByNationalityIgnoreCase("Canadian", Sort.by("name"))
        } returns listOf(director)

        service.findAll(nationality = "Canadian", dir = SortDir.ASC) shouldBe listOf(director)
    }

    @Test
    fun `deleteById should delete director and return true`() {
        val director = oneDirector(id = 1L)
        every { repository.findByIdOrNull(1L) } returns director
        justRun { repository.delete(director) }

        service.deleteById(1L) shouldBe true
        verify(exactly = 1) { repository.delete(director) }
    }
}
