package com.mike.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN("Администратор"),
    SECRETARY("Секретарь"),
    FINANCIER("Финансист"),
    EDITOR("Редактор"),
    USER("Участник");

    private final String role;

    public String getAuthority() {
        return name();
    }

    Role(String string) {
        this.role = string;
    }

    public String getRole() {
        return role;
    }
}
