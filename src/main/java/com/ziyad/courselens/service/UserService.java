package com.ziyad.courselens.service;


import com.ziyad.courselens.domain.dto.UpdateTrackProgramRequest;
import com.ziyad.courselens.domain.dto.UserProfileResponse;
import com.ziyad.courselens.domain.entity.User;
import com.ziyad.courselens.mapper.CourselensMapper;
import com.ziyad.courselens.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;// to access the CRUD methods
    private final CourselensMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email) // take the user from the database
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email)); // if not found
    }


    public UserProfileResponse getCurrentUser() {

        // Step 1 - get email of loggedIn user
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Step 2 - find user in DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 3 - map to DTO and return
        return mapper.toUserProfileResponse(user);
    }

    public UserProfileResponse updateTrackAndProgram(UpdateTrackProgramRequest request) {

        // Step 1 - get email of loggedIn user
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Step 2 - find user in DB
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 3 - update fields
        user.setTrack(request.getTrack());
        user.setProgram(request.getProgram());

        // Step 4 - save and return
        userRepository.save(user);
        return mapper.toUserProfileResponse(user);
    }
}
