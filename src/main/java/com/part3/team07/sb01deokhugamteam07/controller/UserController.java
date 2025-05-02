package com.part3.team07.sb01deokhugamteam07.controller;

import com.part3.team07.sb01deokhugamteam07.dto.user.UserDto;
import com.part3.team07.sb01deokhugamteam07.dto.user.request.UserLoginRequest;
import com.part3.team07.sb01deokhugamteam07.dto.user.request.UserRegisterRequest;
import com.part3.team07.sb01deokhugamteam07.dto.user.request.UserUpdateRequest;
import com.part3.team07.sb01deokhugamteam07.dto.user.response.CursorPageResponsePowerUserDto;
import com.part3.team07.sb01deokhugamteam07.entity.Period;
import com.part3.team07.sb01deokhugamteam07.security.CustomUserDetails;
import com.part3.team07.sb01deokhugamteam07.security.CustomUserDetailsService;
import com.part3.team07.sb01deokhugamteam07.service.DashboardService;
import com.part3.team07.sb01deokhugamteam07.service.UserService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private static final String HEADER = "Deokhugam-Request-User-ID";

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final CustomUserDetailsService userDetailsService;
  private final DashboardService dashboardService;

  /**
   * @methodName : join
   * @date : 2025. 4. 23. 09:43
   * @author : wongil
   * @Description: 유저 회원가입
   **/
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto join(@RequestBody @Validated UserRegisterRequest request) {

    UserDto registeredUser = userService.register(request);
    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(
        registeredUser.id());

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        request.password(),
        userDetails.getAuthorities());
    Authentication authenticate = authenticationManager.authenticate(authToken);

    SecurityContextHolder.getContext().setAuthentication(authenticate);


    return registeredUser;
  }

  /**
   * @methodName : login
   * @date : 2025. 4. 21. 13:20
   * @author : wongil
   * @Description: 유저 로그인
   **/
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<UserDto> loginUser(@RequestBody @Validated UserLoginRequest request) {

    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    // SecurityContext에 저장
    SecurityContextHolder.getContext().setAuthentication(auth);

    UserDto loginUser = userService.login(request);
    return ResponseEntity.ok(loginUser);
  }

  /**
   * @methodName : modify
   * @date : 2025. 4. 22. 11:19
   * @author : wongil
   * @Description: 유저 수정
   **/
  @PatchMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto modify(@RequestBody @Validated UserUpdateRequest request,
      @NotNull @PathVariable("userId") UUID userID,
      @RequestHeader(value = HEADER) UUID authUserId) {

    return userService.update(userID, request);
  }

  @GetMapping("/{userId}")
  @ResponseStatus(HttpStatus.OK)
  public UserDto find(@PathVariable("userId") UUID userId,
      @RequestHeader(value = HEADER) UUID authUserId) {

    return userService.find(userId);
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logicalDelete(@PathVariable("userId") UUID userId,
      @RequestHeader(value = HEADER) UUID authUserId) {

    userService.softDelete(userId);
  }

  @DeleteMapping("/{userId}/hard")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void physicalDelete(@PathVariable("userId") UUID userId,
      @RequestHeader(value = HEADER) UUID authUserId) {

    userService.physicalDelete(userId);
  }

  @GetMapping("/power")
  @ResponseStatus(HttpStatus.OK)
  public CursorPageResponsePowerUserDto findPowerUsers(
      @RequestParam Period period,
      @RequestParam(required = false, defaultValue = "asc") @Pattern(regexp = "(?i)ASC|DESC", message = "direction은 ASC 또는 DESC만 가능합니다.") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(required = false, defaultValue = "50") @Min(1) int limit
  ) {
    return dashboardService.getPowerUsers(period, direction, cursor, after, limit);
  }

}
