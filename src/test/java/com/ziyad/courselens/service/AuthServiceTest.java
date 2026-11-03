package com.ziyad.courselens.service;

import com.ziyad.courselens.config.JwtUtil;
import com.ziyad.courselens.domain.dto.AuthResponse;
import com.ziyad.courselens.domain.dto.LoginRequest;
import com.ziyad.courselens.domain.dto.RegisterRequest;
import com.ziyad.courselens.domain.entity.*;
import com.ziyad.courselens.exception.EmailAlreadyExistsException;
import com.ziyad.courselens.exception.InvalidCredentialsException;
import com.ziyad.courselens.exception.ResourceNotFoundException;
import com.ziyad.courselens.repository.InstitutionRepository;
import com.ziyad.courselens.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private InstitutionRepository institutionRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void register_emailAlreadyExists_throwsException() {
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("something@uoh.edu.sa")
                .build();

        when(userRepository.existsByEmail("something@uoh.edu.sa")).thenReturn(true);
        // Act & assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_domainDoesNotExists_throwsException(){
        //Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("something@ksu.edu.sa")
                .build();

        when(userRepository.existsByEmail("something@ksu.edu.sa")).thenReturn(false);
        when(institutionRepository.findByDomain("ksu.edu.sa")).thenReturn(Optional.empty());

        //Act & assert
        assertThrows(ResourceNotFoundException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_validRequest_savesUserAndReturnsToken(){
        //Arrange
        Institution institution = new Institution();

        RegisterRequest request = RegisterRequest.builder()
                .email("valid@uoh.edu.sa")
                .fullName("testMan") .password("123123")
                .track(Track.BACKEND_DEVELOPER).program(Program.SOFTWARE_ENGINEERING).build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("token").role(Role.STUDENT).track(Track.BACKEND_DEVELOPER).fullName("testMan")
                .program(Program.SOFTWARE_ENGINEERING).build();

        when(userRepository.existsByEmail("valid@uoh.edu.sa")).thenReturn(false);
        when(institutionRepository.findByDomain("uoh.edu.sa")).thenReturn(Optional.of(institution));
        when(jwtUtil.generateToken(any(),anyString(),any())).thenReturn("token");
        when(passwordEncoder.encode("123123")).thenReturn("hashed");

        //Act
        AuthResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        //Assert
        assertEquals(authResponse, response);
        assertEquals("hashed", savedUser.getPassword());
        assertEquals("valid@uoh.edu.sa", savedUser.getEmail());
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertEquals(Track.BACKEND_DEVELOPER, savedUser.getTrack());
        assertEquals(institution, savedUser.getInstitution());
    }

    @Test
    void register_messyEmail_isTrimmedAndLowercased() {
        // Arrange
        Institution institution = new Institution();

        RegisterRequest request = RegisterRequest.builder()
                .email("  Valid@UOH.edu.SA ")
                .fullName("testMan").password("123123")
                .track(Track.BACKEND_DEVELOPER).program(Program.SOFTWARE_ENGINEERING).build();

        // 2. stubs use the CLEAN strings only
        when(userRepository.existsByEmail("valid@uoh.edu.sa")).thenReturn(false);
        when(institutionRepository.findByDomain("uoh.edu.sa")).thenReturn(Optional.of(institution));
        when(passwordEncoder.encode("123123")).thenReturn("hashed");
        when(jwtUtil.generateToken(any(), anyString(), any())).thenReturn("token");

        // Act
        authService.register(request);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("valid@uoh.edu.sa", captor.getValue().getEmail());   // 3. only the email matters here
    }

    @Test
    void login_emailIsNotFound_throwsException(){
        LoginRequest request = LoginRequest.builder()
                .email("something@uoh.edu.sa")
                .build();

        when(userRepository.findByEmail("something@uoh.edu.sa")).thenReturn(Optional.empty());
        // Act & assert
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtUtil, never()).generateToken(any(),anyString(),any());
    }

    @Test
    void login_passwordIsWrong_throwsException(){

        User user = new User();
        user.setEmail("something@uoh.edu.sa");
        user.setPassword("hashed");

        LoginRequest request = LoginRequest.builder()
                .email("something@uoh.edu.sa")
                .password("wrong")
                .build();

        when(userRepository.findByEmail("something@uoh.edu.sa")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong","hashed")).thenReturn(false);

        // Act & assert
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtUtil, never()).generateToken(any(),anyString(),any());
    }

    @Test
    void login_validRequest_returnUserResponse(){
        //Arrange
        Institution institution = new Institution();
        User user = new User();
        user.setId(1L);user.setFullName("testMan"); user.setRole(Role.STUDENT); user.setInstitution(institution);
        user.setEmail("valid@uoh.edu.sa"); user.setPassword("hashed");
        user.setTrack(Track.BACKEND_DEVELOPER); user.setProgram(Program.SOFTWARE_ENGINEERING);

        AuthResponse authResponse = AuthResponse.builder()
                .token("token").role(Role.STUDENT).track(Track.BACKEND_DEVELOPER).fullName("testMan")
                .program(Program.SOFTWARE_ENGINEERING).build();

        LoginRequest request = LoginRequest.builder()
                .email("valid@uoh.edu.sa").password("123").build();

        when(userRepository.findByEmail("valid@uoh.edu.sa")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123","hashed")).thenReturn(true);
        when(jwtUtil.generateToken(user.getId(),user.getRole().name(),user.getInstitution().getId())).thenReturn("token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertEquals(authResponse,response);
    }

    @Test
    void login_messyEmail_isTrimmedAndLowercased(){
        //Arrange
        LoginRequest request = LoginRequest.builder()
                .email("   noTvAlid@uoh.eDu.sa    ").build();

        when(userRepository.findByEmail("notvalid@uoh.edu.sa")).thenReturn(Optional.of(new User()));

        //Act
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        //Assert
        verify(userRepository).findByEmail("notvalid@uoh.edu.sa");
    }
}