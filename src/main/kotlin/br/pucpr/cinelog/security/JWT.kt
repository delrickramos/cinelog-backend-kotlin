package br.pucpr.cinelog.security

import br.pucpr.cinelog.users.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Date

@Component
class JWT(
    val properties: SecurityProperties
) {
    fun createToken(user: User): String =
        UserToken(user).let { userToken ->
            val now = ZonedDateTime.now(ZoneOffset.UTC)
            val hours = if (userToken.isAdmin) properties.adminExpireHours else properties.expireHours
            val expiration = now.plusHours(hours)

            Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(properties.secret.toByteArray()))
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiration.toInstant()))
                .issuer(properties.issuer)
                .subject(userToken.id.toString())
                .claim(
                    USER_FIELD,
                    mapOf(
                        "id" to userToken.id,
                        "name" to userToken.name,
                        "roles" to userToken.roles.toList()
                    )
                )
                .compact()
        }

    fun extract(req: HttpServletRequest): Authentication? {
        try {
            val header = req.getHeader(AUTHORIZATION)
            if (header == null || !header.startsWith(PREFIX)) return null
            val token = header.removePrefix(PREFIX).trim()
            if (token.isBlank()) return null

            val claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(properties.secret.toByteArray()))
                .requireIssuer(properties.issuer)
                .build()
                .parseSignedClaims(token)
                .payload

            val userMap = claims[USER_FIELD] as? Map<*, *> ?: return null
            val id = (userMap["id"] as? Number)?.toLong() ?: claims.subject.toLong()
            val name = userMap["name"]?.toString() ?: ""
            val roles = (userMap["roles"] as? Iterable<*>)
                ?.mapNotNull { it?.toString() }
                ?.toSet()
                ?: emptySet()

            val user = UserToken(id = id, name = name, roles = roles)
            val authorities = user.roles.map { SimpleGrantedAuthority("ROLE_$it") }
            return UsernamePasswordAuthenticationToken.authenticated(user, user.id, authorities)
        } catch (e: Throwable) {
            log.debug("Token rejected", e)
            return null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JWT::class.java)
        private const val PREFIX = "Bearer "
        private const val USER_FIELD = "user"
    }
}
