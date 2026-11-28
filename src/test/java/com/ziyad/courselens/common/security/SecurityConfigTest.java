package com.ziyad.courselens.common.security;

import com.ziyad.courselens.common.domain.Program;
import com.ziyad.courselens.common.domain.Track;
import com.ziyad.courselens.institution.entity.Institution;
import com.ziyad.courselens.institution.repository.InstitutionRepository;
import com.ziyad.courselens.user.entity.User;
import com.ziyad.courselens.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InstitutionRepository institutionRepository;

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withInvalidOrExpiredToken_returns401() throws Exception {
        mockMvc.perform(get("/api/courses").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentToken_onDoctorEndpoint_isStill403() throws Exception {
        Institution institution = institutionRepository.save(new Institution(null, "UOH", "uoh.edu.sa"));
        User student = new User();
        student.setFullName("Student");
        student.setEmail("student@uoh.edu.sa");
        student.setPassword("hashed");
        student.setProgram(Program.SOFTWARE_ENGINEERING);
        student.setTrack(Track.BACKEND_DEVELOPER);
        student.setInstitution(institution);
        userRepository.save(student);
        String token = jwtUtil.generateToken(student.getId(), student.getRole().name(), institution.getId());

        mockMvc.perform(get("/api/courses/my").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
