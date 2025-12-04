package com.binewsian.service;

import com.binewsian.model.User;
import org.springframework.transaction.annotation.Transactional;

public interface RememberMeSvc {
    String createToken(String username);

    User validateTokenAndGetUser(String token);

    @Transactional
    void deleteTokenByUsername(String username);
}
