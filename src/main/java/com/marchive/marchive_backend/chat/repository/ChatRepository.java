package com.marchive.marchive_backend.chat.repository;

import com.marchive.marchive_backend.chat.domain.Chat;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
            SELECT c FROM Chat c
            WHERE c.user.userId = :userId
              AND (:cursor = 0 OR c.chatId < :cursor)
            ORDER BY c.chatId DESC
            """)
    List<Chat> findChatsByCursor(@Param("userId") Long userId,
                                 @Param("cursor") Long cursor,
                                 Pageable pageable);
}
