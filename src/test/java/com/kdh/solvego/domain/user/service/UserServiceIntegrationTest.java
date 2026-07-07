package com.kdh.solvego.domain.user.service;

import com.kdh.solvego.domain.user.dto.SignupRequest;
import com.kdh.solvego.domain.user.dto.SignupResponse;
import com.kdh.solvego.domain.user.entity.User;
import com.kdh.solvego.domain.user.exception.DuplicateUsernameException;
import com.kdh.solvego.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("회원가입에 성공하면 User가 DB에 저장되고 비밀번호가 암호화된다")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("username", "1234");

        // when
        SignupResponse response = userService.signup(request);

        entityManager.flush();
        entityManager.clear();

        // then
        User savedUser = userRepository.findById(response.userId())
                .orElseThrow();

        assertThat(savedUser.getUsername()).isEqualTo("username");

        assertThat(savedUser.matchesPassword("1234", passwordEncoder))
                .isTrue();

        assertThat(savedUser.matchesPassword("wrong-password", passwordEncoder))
                .isFalse();
    }

    @Test
    @DisplayName("중복된 username으로 회원가입하면 예외가 발생한다")
    void signup_fails_when_username_is_duplicated() {
        // given
        userService.signup(new SignupRequest("username", "1234"));

        SignupRequest duplicatedRequest = new SignupRequest("username", "5678");

        // when & then
        assertThatThrownBy(() -> userService.signup(duplicatedRequest))
                .isInstanceOf(DuplicateUsernameException.class);
    }
}