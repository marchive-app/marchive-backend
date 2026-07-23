package com.marchive.marchive_backend.auth.repository;

import com.marchive.marchive_backend.auth.domain.RefreshToken;
import com.marchive.marchive_backend.auth.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
