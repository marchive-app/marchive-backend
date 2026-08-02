package com.marchive.marchive_backend.chat.repository;

import com.marchive.marchive_backend.chat.domain.Message;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // 특정 채팅방의 메시지를 최신순으로. 커서 기반 페이지네이션
    @Query("""
            SELECT m FROM Message m
            WHERE m.chat.chatId = :chatId
              AND (:cursor = 0 OR m.messageId < :cursor)
            ORDER BY m.messageId DESC
            """)
    List<Message> findMessagesByCursor(@Param("chatId") Long chatId,
                                       @Param("cursor") Long cursor,
                                       Pageable pageable);

    // 채팅 목록 조회 시, 각 채팅방의 최신 메시지 몇 개를 미리 가져오기 위한 용도
    @Query("""
            SELECT m FROM Message m
            WHERE m.chat.chatId = :chatId
            ORDER BY m.messageId DESC
            """)
    List<Message> findRecentMessages(@Param("chatId") Long chatId, Pageable pageable);
}
