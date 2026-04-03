package com.sp.app.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.web.bind.annotation.GetMapping; 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.LoginUser;
import com.sp.app.domain.dto.TokenRequestDto;
import com.sp.app.security.JwtToken;
import com.sp.app.service.AuthService;
import com.sp.app.repository.MemberRepository; 

import jakarta.persistence.EntityManager; 
import jakarta.persistence.Query; 
import jakarta.servlet.http.HttpServletRequest; 
import lombok.Data; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api")
@Slf4j
public class AuthRestController {
    
    private final AuthService authService;
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    // ────────────────────────────────────────────────────────
    // 기존에 있던 로그인(signin) 및 토큰 재발급(refresh) 로직 (수정 없음)
    // ────────────────────────────────────────────────────────
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, String> requestMap) {
        try {
            String username = requestMap.get("login_id");
            String password = requestMap.get("password");
            
            JwtToken jwtToken = authService.login(username, password);
            LoginUser loginUser = authService.getLoginUser(jwtToken.getAccessToken());
            
            return ResponseEntity.ok(Map.of(
                "accessToken", jwtToken.getAccessToken(), 
                "refreshToken", jwtToken.getRefreshToken(),
                "role", loginUser.getRole(),
                "member_id", loginUser.getMember_id(),
                "name", loginUser.getName() // 여기서 프론트의 '닉네임'이 응답됩니다!
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 정보가 올바르지 않습니다.");
        }
    }

    @PostMapping("refresh")
    public ResponseEntity<?> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        try {
            return ResponseEntity.ok(authService.reissue(tokenRequestDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("세션이 만료되었습니다. 다시 로그인하세요.");
        }
    }


    // 1. 아이디 중복 확인
    @GetMapping("/userIdCheck")
    public Map<String, Boolean> checkId(@RequestParam("login_id") String loginId) {
        boolean exists = memberRepository.findByLoginId(loginId).isPresent();
        return Map.of("exists", exists);
    }

    // 2. 풀옵션 회원가입 (member1, member2 처리)
    @PostMapping("/signup")
    @Transactional
    public ResponseEntity<?> signup(@RequestBody SignupRequest req, HttpServletRequest request) {
        
        String encPwd = passwordEncoder.encode(req.getPassword());

        Query seqQuery = em.createNativeQuery("SELECT member_seq.NEXTVAL FROM DUAL");
        Long newMemberId = ((Number) seqQuery.getSingleResult()).longValue();

        // member1 삽입
        em.createNativeQuery("INSERT INTO member1 (member_id, login_id, password, enabled, user_level, created_at, update_at, failure_cnt) " +
                             "VALUES (?, ?, ?, 1, 1, SYSDATE, SYSDATE, 0)")
          .setParameter(1, newMemberId)
          .setParameter(2, req.getLogin_id())
          .setParameter(3, encPwd)
          .executeUpdate();

        // IP 추출
        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null) ipAddr = request.getRemoteAddr();

        // member2 삽입 (입력 안 한 값은 자동으로 NULL 처리됨)
        em.createNativeQuery("INSERT INTO member2 " +
                             "(member_id, name, birth, profile_photo, tel, zip, addr1, addr2, email, receive_email, ip_addr) " +
                             "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), NULL, ?, ?, ?, ?, ?, 1, ?)")
          .setParameter(1, newMemberId)
          .setParameter(2, req.getName()) // 화면의 '닉네임'
          .setParameter(3, (req.getBirth() == null || req.getBirth().isEmpty()) ? null : req.getBirth())
          .setParameter(4, req.getTel())
          .setParameter(5, req.getZip())
          .setParameter(6, req.getAddr1())
          .setParameter(7, req.getAddr2())
          .setParameter(8, req.getEmail())
          .setParameter(9, ipAddr)
          .executeUpdate();

        // 권한 부여
        em.createNativeQuery("INSERT INTO member_authority (login_id, authority) VALUES (?, 'ROLE_USER')")
          .setParameter(1, req.getLogin_id())
          .executeUpdate();

        return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
    }

    // 통신용 DTO (내부 클래스로 추가)
    @Data
    public static class SignupRequest {
        private String login_id;
        private String password;
        private String name;
        private String email;
        private String birth;
        private String tel;
        private String zip;
        private String addr1;
        private String addr2;
    }
}