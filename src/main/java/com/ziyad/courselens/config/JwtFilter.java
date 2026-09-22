package com.ziyad.courselens.config;

import com.ziyad.courselens.domain.entity.User;
import com.ziyad.courselens.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // to inject the final filed
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; // to use the verifying methods we create
    private final UserService userService; // if everything works fine we take user data from this

    @Override
    protected void doFilterInternal(HttpServletRequest request,// the req we will be dealing with
                                    HttpServletResponse response,// our response
                                    FilterChain filterChain) // the chain we are going to walk through to filter the req
            throws ServletException, IOException { // tell spring about this EXP for him to handle

        String authHeader = request.getHeader("Authorization");// grabbing the auth from the req header and store it here

        // if the header is empty then no token is there, and if the header doesn't start with bearer then there is something wrong
        // we treat this like there is NO TOKEN
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // pass forward and let SecurityConfig decide if they're allowed or not
            return; // we don't need more filtering
        }

        // to extract the token
        // bearer have 6 characters and the space is the 7th, we need everything after that ( the token itself )
        String token = authHeader.substring(7);

        // check if the token is valid
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response); // do filter and let security handle it
            return; // stop filtering
        }

        // take the id from the token
        Long userId = jwtUtil.extractUserId(token);
        // If the token gave us a valid user, AND nobody has been logged in for this request yet — then log this user in for this request.
        // the second condition will never happen if i only use JWT but it may happen if i add Oauth2
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userService.loadUserById(userId);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());// 1- the principle 2- password 3- the role
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));// extra details to the authentication object
            SecurityContextHolder.getContext().setAuthentication(auth); // to set the identity of this request
        }

        // passes the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}