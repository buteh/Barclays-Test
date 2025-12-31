package com.eaglebank.user.api.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService)
	{
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException
	{
		// If already authenticated, don’t override
		if (SecurityContextHolder.getContext().getAuthentication() != null)
		{
			chain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Bearer "))
		{
			chain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);

		try
		{
			String userId = jwtService.parseUserId(token);

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
					Collections.emptyList());

			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);

			chain.doFilter(request, response);
		}
		catch (Exception e)
		{
			// Invalid token -> treat as unauthenticated
			SecurityContextHolder.clearContext();
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

}
