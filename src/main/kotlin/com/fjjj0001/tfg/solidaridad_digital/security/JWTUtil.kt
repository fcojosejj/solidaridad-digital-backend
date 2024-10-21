package com.fjjj0001.tfg.solidaridad_digital.security
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*

@Component
class JWTUtil {
    var secret = "tfg-solidaridad-digital"
    var expiration = 3600000

    fun generateToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        val username = getEmailFromToken(token)
        return username.isNotEmpty() && !isTokenExpired(token)
    }

    fun getEmailFromToken(token: String): String {
        return getClaimFromToken(token, Claims::getSubject)
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = getClaimFromToken(token, Claims::getExpiration)
        return expiration.before(Date())
    }

    private fun <T> getClaimFromToken(token: String, claimsResolver: (Claims) -> T): T {
        val claims = getAllClaimsFromToken(token)
        return claimsResolver(claims)
    }

    private fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body

    }
}