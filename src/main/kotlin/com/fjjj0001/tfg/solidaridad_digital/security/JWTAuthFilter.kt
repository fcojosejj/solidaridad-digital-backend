package com.fjjj0001.tfg.solidaridad_digital.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JWTAuthFilter(
    private val jwtUtil: JWTUtil,
    private val userDetailsService: UserDetailsService
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val header = request.getHeader("Authorization")
            if (header != null && header.startsWith("Bearer ")) {
                val token = header.substring(7)
                val email = jwtUtil.getEmailFromToken(token)

                if (SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userDetailsService.loadUserByUsername(email)
                    if (jwtUtil.validateToken(token)) {
                        val authToken = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                        authToken.details =
                            org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authToken
                    }
                }
            }
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
        }
    }


}