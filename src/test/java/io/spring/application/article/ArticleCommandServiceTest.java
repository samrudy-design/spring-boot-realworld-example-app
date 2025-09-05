package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({ArticleCommandService.class, MyBatisUserRepository.class, MyBatisArticleRepository.class})
public class ArticleCommandServiceTest extends DbTestBase {
  @Autowired private ArticleCommandService articleCommandService;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    userRepository.save(user);
  }

  @Test
  public void should_create_article_success() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = articleCommandService.createArticle(param, user);

    Assertions.assertNotNull(article.getId());
    Assertions.assertEquals("Test Article", article.getTitle());
    Assertions.assertEquals("Test Description", article.getDescription());
    Assertions.assertEquals("Test Body", article.getBody());
    Assertions.assertTrue(article.getTags().contains(new Tag("java")));
    Assertions.assertTrue(article.getTags().contains(new Tag("spring")));
    Assertions.assertEquals(user.getId(), article.getUserId());

    Optional<Article> saved = articleRepository.findById(article.getId());
    Assertions.assertTrue(saved.isPresent());
    Assertions.assertEquals(article.getTitle(), saved.get().getTitle());
  }

  @Test
  public void should_update_article_success() {
    Article article =
        new Article(
            "Original Title",
            "Original Desc",
            "Original Body",
            Arrays.asList("tag1"),
            user.getId());
    articleRepository.save(article);

    UpdateArticleParam updateParam =
        new UpdateArticleParam("Updated Title", "Updated Body", "Updated Description");

    Article updated = articleCommandService.updateArticle(article, updateParam);

    Assertions.assertEquals("Updated Title", updated.getTitle());
    Assertions.assertEquals("Updated Description", updated.getDescription());
    Assertions.assertEquals("Updated Body", updated.getBody());

    Optional<Article> saved = articleRepository.findById(article.getId());
    Assertions.assertTrue(saved.isPresent());
    Assertions.assertEquals("Updated Title", saved.get().getTitle());
  }
}
