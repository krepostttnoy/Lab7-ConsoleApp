package org.example.token

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import java.sql.Time

class JWTManager {
    private val bytes = byteArrayOf(11, 51, -117, 10, 106, -108, 33, 99, 1, 11, 75, 28, 9, -61, 47, 53, -73, -23, -114, 29, 123, -87, 3, -24, -118, -88, 124, 81, -113, -58, -104, 81)
    private val key = Keys.hmacShaKeyFor(this.bytes)

    fun createJWS(issuer: String, user: String) : String {
        val date = Time(System.currentTimeMillis() + 300000) // 5 minutes
        return Jwts.builder().setIssuer(issuer).setSubject(user).setExpiration(date).signWith(key).compact()
    }

    fun validateJWS(jws : String) : Boolean {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws)
            if (claims.body.expiration < Time(System.currentTimeMillis())) {
                return false
            }
            true
        } catch (e:JwtException) {
            false
        }
    }

    fun retrieveUsername(jws: String) : String {
        return try {
            val claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws)
            claims.body.subject
        } catch (_: Exception) {
            ""
        }

    }
}