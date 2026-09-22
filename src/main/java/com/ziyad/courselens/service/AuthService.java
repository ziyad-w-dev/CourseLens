package com.ziyad.courselens.service;

import com.ziyad.courselens.domain.dto.*;
import com.ziyad.courselens.domain.entity.Institution;
import com.ziyad.courselens.domain.entity.Role;
import com.ziyad.courselens.domain.entity.Track;
import com.ziyad.courselens.domain.entity.User;
import com.ziyad.courselens.exception.EmailAlreadyExistsException;
import com.ziyad.courselens.exception.InvalidCredentialsException;
import com.ziyad.courselens.exception.ResourceNotFoundException;
import com.ziyad.courselens.repository.InstitutionRepository;
import com.ziyad.courselens.repository.UserRepository;
import com.ziyad.courselens.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final InstitutionRepository institutionRepository;

    public AuthResponse register(RegisterRequest request) {

        // 1 - email already taken?
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already in use");
        }

        // 2 - is the email domain a registered institution?
        String domain = request.getEmail().substring(request.getEmail().indexOf("@") + 1);
        Institution institution = institutionRepository.findByDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("No registered institution for domain: " + domain));

        // --- Passed all checks, now build ---

        User user = new User();

        // track: students with no track default to NOT_SURE; doctors stay null
        if (request.getRole() == Role.STUDENT && request.getTrack() == null) {
            user.setTrack(Track.NOT_SURE);
        } else {
            user.setTrack(request.getTrack());
        }

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole()); // TODO: SECURITY - role comes from request body, fix before production (privilege escalation)
        user.setProgram(request.getProgram());
        user.setInstitution(institution);

        // save
        userRepository.save(user);

        // token
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name(), institution.getId());
        return new AuthResponse(token, user.getRole(), user.getTrack());
    }

    public AuthResponse login(LoginRequest request) {

        // Step 1 - find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // Step 2 - check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Step 3 - generate token and return
        String token = jwtUtil.generateToken(user.getId(), user.getRole().name(),user.getInstitution().getId());
        return new AuthResponse(token, user.getRole(),user.getTrack());
    }
}