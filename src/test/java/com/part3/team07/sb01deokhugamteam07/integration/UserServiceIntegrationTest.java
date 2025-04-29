package com.part3.team07.sb01deokhugamteam07.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.part3.team07.sb01deokhugamteam07.dto.user.UserDto;
import com.part3.team07.sb01deokhugamteam07.entity.User;
import com.part3.team07.sb01deokhugamteam07.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc()
@ActiveProfiles("test")
@AutoConfigureTestEntityManager
public class UserServiceIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  @Autowired
  private TestEntityManager em;

  @Test
  @DisplayName("GET /api/users/{userId} - Deokhugam-Request-User-ID 응답 헤더 확인")
  void checkUserLoginWithResponseHeader() throws Exception {

    User user = em.persist(new User("test", "password1234", "test@mail.com"));

    UserDto userDto = userService.find(user.getId());
    String userJson = objectMapper.writeValueAsString(userDto);

    mockMvc.perform(get("/api/users/{userId}", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Deokhugam-Request-User-ID", user.getId()))
        .andExpect(status().isOk())
        .andExpect(content().json(userJson))
        .andExpect(header().string("Deokhugam-Request-User-ID", user.getId().toString()));

  }
}
