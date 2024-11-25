package com.developkim.markethub.user.controller;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import com.developkim.markethub.jwt.JwtUtil;
import com.developkim.markethub.user.dto.Login;
import com.developkim.markethub.user.dto.SignUp;
import com.developkim.markethub.user.entity.User;
import com.developkim.markethub.user.service.CustomUserDetailsService;
import com.developkim.markethub.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwt;

    @PostMapping("/signUp")
    public ResponseEntity<User> create(@Valid @RequestBody SignUp signUp) {
        return ResponseEntity.status(OK).body(userService.create(signUp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.status(OK).build();
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody Login login, HttpServletResponse response) {
        Authentication authentication = authenticateUser(login);

        String token = jwt.generateToken(((UserDetails) authentication.getPrincipal()).getUsername());
        response.addCookie(generateCookie(token));

        return token;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = getTokenFromRequest(request);
        if (token != null && jwt.validateToken(token)) {
            clearTokenCookie(response);
            return ResponseEntity.status(OK).build();
        }
        return ResponseEntity.status(FORBIDDEN).build();
    }

    @PostMapping("/token")
    @ResponseStatus(OK)
    public void jwtValidate(@RequestParam String token) {
        if (!jwt.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token is not validation");
        }
    }

    private Authentication authenticateUser(Login login) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getName(), login.getPassword())
        );
    }

    private static Cookie generateCookie(String token) {
        Cookie cookie = new Cookie("market_token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        return cookie;
    }

    private static void clearTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("market_token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "market_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
