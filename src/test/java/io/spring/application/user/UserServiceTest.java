package io.spring.application.user;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

@Import({UserService.class, MyBatisUserRepository.class, BCryptPasswordEncoder.class})
@TestPropertySource(
    properties = {"image.default=https://static.productionready.io/images/smiley-cyrus.jpg"})
public class UserServiceTest extends DbTestBase {
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Test
  public void should_create_user_success() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password");

    User user = userService.createUser(registerParam);

    Assertions.assertNotNull(user.getId());
    Assertions.assertEquals("test@example.com", user.getEmail());
    Assertions.assertEquals("testuser", user.getUsername());
    Assertions.assertTrue(passwordEncoder.matches("password", user.getPassword()));
    Assertions.assertEquals(
        "https://static.productionready.io/images/smiley-cyrus.jpg", user.getImage());

    Optional<User> saved = userRepository.findById(user.getId());
    Assertions.assertTrue(saved.isPresent());
    Assertions.assertEquals(user.getEmail(), saved.get().getEmail());
  }

  @Test
  public void should_update_user_success() {
    User user = new User("original@example.com", "original", "password", "bio", "image");
    userRepository.save(user);

    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("updated@example.com")
            .username("updated")
            .password("newpassword")
            .bio("updated bio")
            .image("updated image")
            .build();

    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);
    userService.updateUser(command);

    Optional<User> updated = userRepository.findById(user.getId());
    Assertions.assertTrue(updated.isPresent());
    Assertions.assertEquals("updated@example.com", updated.get().getEmail());
    Assertions.assertEquals("updated", updated.get().getUsername());
    Assertions.assertEquals("updated bio", updated.get().getBio());
    Assertions.assertEquals("updated image", updated.get().getImage());
  }
}
