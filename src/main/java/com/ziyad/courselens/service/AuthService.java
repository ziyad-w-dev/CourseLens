package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.Institution;
import com.ziyad.courselens.domain.entity.User;
import com.ziyad.courselens.exception.EmailAlreadyExistsException;
import com.ziyad.courselens.exception.InvalidCredentialsException;
import com.ziyad.courselens.exception.ResourceNotFoundException;
import com.ziyad.courselens.repository.InstitutionRepository;
import com.ziyad.courselens.repository.UserRepository;
import com.ziyad.courselens.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final InstitutionRepository institutionRepository;

    public AuthResponse register(RegisterRequest request) {

        // trim to remove any additional spaces
        String email = request.getEmail().trim().toLowerCase();

        // 1 - email already taken?
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // 2 - is the email domain a registered institution?
        String domain = email.substring(email.indexOf("@") + 1);
        Institution institution = institutionRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("No registered institution for domain: " + domain));


        // --- Passed all checks, now build ---

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setTrack(request.getTrack());
        user.setProgram(request.getProgram());
        user.setInstitution(institution);

        // save
        userRepository.save(user);

        // token + what the frontend header needs (name, program)
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name(), institution.getId());
        return new AuthResponse(token, user.getRole(), user.getTrack(),
                user.getFullName(), user.getProgram());
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // Step 1 - find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Step 2 - check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Step 3 - generate token and return it with what the frontend header needs
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name(), user.getInstitution().getId());
        return new AuthResponse(token, user.getRole(), user.getTrack(),
                user.getFullName(), user.getProgram());
    }
}