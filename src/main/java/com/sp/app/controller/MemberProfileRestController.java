package com.sp.app.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberProfileRestController {

    private final PasswordEncoder passwordEncoder;
    private final EntityManager em;

    // 1. 내 정보 불러오기 (마이페이지 진입 시 자동 호출)
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        // 시큐리티 컨텍스트에서 현재 로그인한 유저의 아이디(login_id) 추출
        String loginId = authentication.getName(); 
        
        // member1과 member2 테이블을 조인하여 필요한 정보만 쏙 빼오는 쿼리
        String sql = "SELECT m1.login_id, m2.name, m2.email, TO_CHAR(m2.birth, 'YYYY-MM-DD') as birth, " +
                     "m2.tel, m2.zip, m2.addr1, m2.addr2, m2.profile_photo " +
                     "FROM member1 m1 LEFT JOIN member2 m2 ON m1.member_id = m2.member_id " +
                     "WHERE m1.login_id = ?";
                     
        Object[] result = (Object[]) em.createNativeQuery(sql)
                                       .setParameter(1, loginId)
                                       .getSingleResult();
                                       
        // 조회한 데이터를 프론트로 보낼 DTO에 담기
        ProfileDto dto = new ProfileDto();
        dto.setLogin_id((String) result[0]);
        dto.setName((String) result[1]);
        dto.setEmail((String) result[2]);
        dto.setBirth((String) result[3]);
        dto.setTel((String) result[4]);
        dto.setZip((String) result[5]);
        dto.setAddr1((String) result[6]);
        dto.setAddr2((String) result[7]);
        dto.setProfile_photo((String) result[8]);
        
        return ResponseEntity.ok(dto);
    }

    // 2. 내 정보 수정하기 ('정보 수정하기' 버튼 클릭 시 호출)
    @PutMapping("/update")
    @Transactional // 도중에 에러가 나면 롤백되도록 설정
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequest req, Authentication authentication) {
        String loginId = authentication.getName();
        
        // 1. member_id(PK) 먼저 가져오기
        Number memberIdObj = (Number) em.createNativeQuery("SELECT member_id FROM member1 WHERE login_id = ?")
                                        .setParameter(1, loginId)
                                        .getSingleResult();
        Long memberId = memberIdObj.longValue();

        // 2. 새 비밀번호를 입력했을 경우에만 member1 테이블(비밀번호) 업데이트
        if (req.getNewPassword() != null && !req.getNewPassword().trim().isEmpty()) {
            String encPwd = passwordEncoder.encode(req.getNewPassword());
            em.createNativeQuery("UPDATE member1 SET password = ?, update_at = SYSDATE WHERE member_id = ?")
              .setParameter(1, encPwd)
              .setParameter(2, memberId)
              .executeUpdate();
        }

        // 3. 상세 정보(member2 테이블) 일괄 업데이트
        String updateSql = "UPDATE member2 " +
                           "SET name=?, email=?, tel=?, zip=?, addr1=?, addr2=?, birth=TO_DATE(?, 'YYYY-MM-DD') " +
                           "WHERE member_id=?";
                           
        em.createNativeQuery(updateSql)
          .setParameter(1, req.getName())
          .setParameter(2, req.getEmail())
          .setParameter(3, req.getTel())
          .setParameter(4, req.getZip())
          .setParameter(5, req.getAddr1())
          .setParameter(6, req.getAddr2())
          // 생년월일을 입력 안 한 경우 NULL로 안전하게 들어가게 처리
          .setParameter(7, (req.getBirth() == null || req.getBirth().isEmpty()) ? null : req.getBirth())
          .setParameter(8, memberId)
          .executeUpdate();

        return ResponseEntity.ok(Map.of("message", "회원 정보가 성공적으로 수정되었습니다."));
    }

    // ────────────────────────────────────────────────────────
    // 통신용 DTO 모음 (프론트엔드와 주고받는 데이터 규격)
    // ────────────────────────────────────────────────────────
    @Data
    public static class ProfileDto {
        private String login_id;
        private String name;
        private String email;
        private String birth;
        private String tel;
        private String zip;
        private String addr1;
        private String addr2;
        private String profile_photo;
    }

    @Data
    public static class ProfileUpdateRequest {
        private String name;
        private String email;
        private String birth;
        private String tel;
        private String zip;
        private String addr1;
        private String addr2;
        private String newPassword;
    }
}