package com.ziyad.courselens.config;

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

        // take the email from the token
        String email = jwtUtil.extractEmail(token);

        // if email exists AND no identity has been set yet for this request
        // SecurityContextHolder holds the identity of whoever is making the request
        // if null then we should set the identity if not null we don't have to do it again
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // UserDetails: spring security interface that holds the user info
            UserDetails userDetails = userService.loadUserByUsername(email);// take the email from the token and fetch the user data
            // spring security class
            UsernamePasswordAuthenticationToken auth =
                    // 1- who the person is      2- the password( null already in the token )       3- their role
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));// extra details to the authentication object
            SecurityContextHolder.getContext().setAuthentication(auth); // to set the identity of this request
        }

        // passes the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}