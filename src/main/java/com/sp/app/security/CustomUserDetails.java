package com.sp.app.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sp.app.domain.dto.LoginUser;

public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final LoginUser member;
	private final List<String> roles;
	private final boolean disabled;

	private CustomUserDetails(Builder builder) {
		this.member = builder.member;
		this.roles = builder.roles;
		this.disabled = builder.disabled;
	}

	public static class Builder {
		private LoginUser member;
		private List<String> roles;
		private boolean disabled = false;

		public Builder loginUser(LoginUser member) {
			this.member = member;
			return this;
		}

		public Builder roles(List<String> roles) {
			this.roles = roles;
			return this;
		}

		public Builder disabled(boolean disabled) {
			this.disabled = disabled;
			return this;
		}

		public CustomUserDetails build() {
			if (this.member == null) {
				throw new IllegalStateException("SessionInfo 객체는 필수입니다.");
			}
			return new CustomUserDetails(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream()
			.map(role -> new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role))
			.collect(Collectors.toList());
	}

	@Override
	public String getPassword() { return member.getPassword(); }

	@Override
	public String getUsername() { return member.getLogin_id(); }

	@Override
	public boolean isEnabled() { return !disabled; }

	@Override public boolean isAccountNonExpired() { return true; }
	@Override public boolean isAccountNonLocked() { return true; }
	@Override public boolean isCredentialsNonExpired() { return true; }

	// ✅ 수정: 반환 타입 Long → LoginUser
	public LoginUser getLoginUser() { return member; }

	// ✅ 추가: WorkspaceRestController의 getMemberId()에서 사용
	public Long getMemberId() { return member.getMember_id(); }
}