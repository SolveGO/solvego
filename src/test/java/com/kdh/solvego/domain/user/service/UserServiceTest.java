package com.kdh.solvego.domain.user.service;


import com.kdh.solvego.domain.user.dto.SignupRequest;
import com.kdh.solvego.domain.user.dto.SignupResponse;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.exception.DuplicateUsernameException;
import com.kdh.solvego.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입에 성공한다")
    void signup_success(){
        // given
        SignupRequest request = new SignupRequest("username", "1234");

        when(userRepository.existsByUsername("username"))
                .thenReturn(false);

        when(passwordEncoder.encode("1234"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->{
                    User savedUser = invocation.getArgument(0);
                    ReflectionTestUtils.setField(savedUser,"id",1L);
                    return savedUser;
                });

        // when
        SignupResponse response = userService.signup(request);

        //then
        assertThat(response.userId()).isEqualTo(1L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("username");

        String savedPassword = (String) ReflectionTestUtils.getField(savedUser, "password");
        assertThat(savedPassword).isEqualTo("encoded-password");


        verify(userRepository).existsByUsername("username");
        verify(passwordEncoder).encode("1234");
    }

    @Test
    @DisplayName("중복된 username이면 예외가 발생한다")
    void duplicate_username_throws_exception() {
        // given
        SignupRequest request = new SignupRequest("username", "1234");

        when(userRepository.existsByUsername("username"))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository).existsByUsername("username");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}
