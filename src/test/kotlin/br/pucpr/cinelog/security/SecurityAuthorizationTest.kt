package br.pucpr.cinelog.security

import br.pucpr.cinelog.directors.Director
import br.pucpr.cinelog.directors.DirectorRepository
import br.pucpr.cinelog.genres.Genre
import br.pucpr.cinelog.genres.GenreRepository
import br.pucpr.cinelog.movies.Movie
import br.pucpr.cinelog.movies.MovieRepository
import br.pucpr.cinelog.reviews.Review
import br.pucpr.cinelog.reviews.ReviewRepository
import br.pucpr.cinelog.users.User
import br.pucpr.cinelog.users.UserRepository
import jakarta.servlet.Filter
import jakarta.transaction.Transactional
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Transactional
class SecurityAuthorizationTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private lateinit var springSecurityFilterChain: Filter

    @Autowired
    private lateinit var jwt: JWT

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var movieRepository: MovieRepository

    @Autowired
    private lateinit var directorRepository: DirectorRepository

    @Autowired
    private lateinit var genreRepository: GenreRepository

    @Autowired
    private lateinit var reviewRepository: ReviewRepository

    private lateinit var mockMvc: MockMvc
    private lateinit var admin: User
    private lateinit var userA: User
    private lateinit var userB: User
    private lateinit var adminToken: String
    private lateinit var userAToken: String
    private lateinit var userBToken: String
    private lateinit var movie: Movie
    private lateinit var director: Director
    private lateinit var genre: Genre
    private lateinit var reviewOfA: Review

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilters<DefaultMockMvcBuilder>(springSecurityFilterChain)
            .build()

        admin = userRepository.findByEmail("admin@cinelog.com")!!
        userA = userRepository.findByEmail("delrick@cinelog.com")!!
        userB = userRepository.save(
            User(
                name = "Other User",
                email = "other@cinelog.com",
                password = "password123"
            )
        )

        adminToken = "Bearer ${jwt.createToken(admin)}"
        userAToken = "Bearer ${jwt.createToken(userA)}"
        userBToken = "Bearer ${jwt.createToken(userB)}"

        director = directorRepository.save(
            Director(
                name = "Test Director",
                nationality = "Brazilian",
                birthYear = 1980,
                biography = "Test director biography"
            )
        )
        genre = genreRepository.save(
            Genre(
                name = "Noir",
                description = "Classic film noir"
            )
        )
        movie = movieRepository.save(
            Movie(
                title = "Test Movie",
                synopsis = "A detailed synopsis for security testing purposes.",
                releaseYear = 2020,
                durationMinutes = 120,
                director = director,
                genres = mutableSetOf(genre)
            )
        )
        reviewOfA = reviewRepository.save(
            Review(
                rating = 9,
                comment = "Great movie!",
                user = userA,
                movie = movie
            )
        )
    }

    @Test
    fun `public GET endpoints should be accessible without authentication`() {
        mockMvc.perform(get("/health")).andExpect(status().isOk)
        mockMvc.perform(get("/movies")).andExpect(status().isOk)
        mockMvc.perform(get("/directors")).andExpect(status().isOk)
        mockMvc.perform(get("/genres")).andExpect(status().isOk)
        mockMvc.perform(get("/reviews")).andExpect(status().isOk)
        mockMvc.perform(get("/users")).andExpect(status().isOk)
        mockMvc.perform(get("/roles")).andExpect(status().isOk)
    }

    @Test
    fun `unauthenticated write requests should return 401 UNAUTHORIZED`() {
        mockMvc.perform(
            post("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"New","synopsis":"Valid synopsis text","releaseYear":2022,"durationMinutes":100}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/directors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"New Dir","nationality":"US","birthYear":1975,"biography":"Bio text"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/genres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Western","description":"Western movies"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"CURATOR","description":"Catalog curator"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            post("/movies/${movie.id}/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":8,"comment":"Nice movie!"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            patch("/reviews/${reviewOfA.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":5}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(delete("/reviews/${reviewOfA.id}"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(delete("/movies/${movie.id}"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(
            patch("/users/${userA.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Anon"}""")
        ).andExpect(status().isUnauthorized)

        mockMvc.perform(
            put("/users/${userA.id}/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bio":"Anon bio"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `non-admin authenticated user should receive 403 FORBIDDEN on catalog and admin write routes`() {
        mockMvc.perform(
            post("/movies")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Unauthorized Movie","synopsis":"Valid synopsis text","releaseYear":2022,"durationMinutes":100}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            put("/movies/${movie.id}")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Changed","synopsis":"Valid synopsis text","releaseYear":2022,"durationMinutes":100}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/movies/${movie.id}")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Changed"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(delete("/movies/${movie.id}").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(put("/movies/${movie.id}/genres/Noir").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/movies/${movie.id}/genres/Noir").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(put("/movies/${movie.id}/director/${director.id}").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(delete("/movies/${movie.id}/director").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/directors")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"New Dir","nationality":"US","birthYear":1975,"biography":"Bio text"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(delete("/directors/${director.id}").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/genres")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Western","description":"Western movies"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(delete("/genres/${genre.id}").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)

        mockMvc.perform(
            post("/roles")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"CURATOR","description":"Catalog curator"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(delete("/users/${userB.id}").header(AUTHORIZATION, userAToken))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `ADMIN should be allowed to manage catalog resources`() {
        mockMvc.perform(
            post("/genres")
                .header(AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Thriller","description":"Suspenseful movies"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/directors")
                .header(AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Bong Joon-ho","nationality":"South Korean","birthYear":1969,"biography":"Director of Parasite"}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/movies")
                .header(AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"Parasite","synopsis":"Greed and class discrimination threaten a symbiotic relationship.","releaseYear":2019,"durationMinutes":132}""")
        ).andExpect(status().isCreated)

        mockMvc.perform(delete("/movies/${movie.id}/genres/Noir").header(AUTHORIZATION, adminToken))
            .andExpect(status().isOk)

        mockMvc.perform(delete("/movies/${movie.id}/director").header(AUTHORIZATION, adminToken))
            .andExpect(status().isOk)
    }

    @Test
    fun `non-admin user cannot create, edit or delete another user's review`() {
        mockMvc.perform(
            post("/movies/${movie.id}/reviews")
                .header(AUTHORIZATION, userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":${userA.id},"rating":1,"comment":"Impersonating user A"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":1,"comment":"Tampered"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            delete("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, userBToken)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `review owner can create, edit and delete their own review`() {
        mockMvc.perform(
            post("/movies/${movie.id}/reviews")
                .header(AUTHORIZATION, userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":8,"comment":"Loved the cinematography"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").value(userB.id))

        mockMvc.perform(
            patch("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"rating":10,"comment":"Updated review"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.rating").value(10))

        mockMvc.perform(
            delete("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, userAToken)
        ).andExpect(status().isOk)
    }

    @Test
    fun `ADMIN can edit and delete another user's review`() {
        mockMvc.perform(
            patch("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"comment":"Moderated by admin"}""")
        ).andExpect(status().isOk)

        mockMvc.perform(
            delete("/reviews/${reviewOfA.id}")
                .header(AUTHORIZATION, adminToken)
        ).andExpect(status().isOk)
    }

    @Test
    fun `non-admin user cannot update another user's name or profile, but can update their own`() {
        mockMvc.perform(
            patch("/users/${userA.id}")
                .header(AUTHORIZATION, userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Hacked"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            put("/users/${userA.id}/profile")
                .header(AUTHORIZATION, userBToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bio":"Hacked bio"}""")
        ).andExpect(status().isForbidden)

        mockMvc.perform(
            patch("/users/${userA.id}")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Delrick Updated"}""")
        ).andExpect(status().isOk)

        mockMvc.perform(
            put("/users/${userA.id}/profile")
                .header(AUTHORIZATION, userAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bio":"Updated bio"}""")
        ).andExpect(status().isOk)
    }
}
