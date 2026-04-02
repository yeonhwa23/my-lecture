package com.sp.app.repository;

import com.sp.app.entity.member.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    // PK가 login_id(String) → findById(), save(), deleteById() 기본 메서드로 충분
}
