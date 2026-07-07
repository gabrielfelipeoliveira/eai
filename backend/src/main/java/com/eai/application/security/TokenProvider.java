package com.eai.application.security;

import com.eai.domain.user.User;

public interface TokenProvider {

    String createAccessToken(User user);

    AuthenticatedUser parseAccessToken(String token);
}
