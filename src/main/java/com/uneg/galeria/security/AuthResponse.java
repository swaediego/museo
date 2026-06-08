package com.uneg.galeria.security;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}