package com.marchive.marchive_backend.chat.service;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.chat.domain.Chat;
import com.marchive.marchive_backend.chat.domain.Message;
import com.marchive.marchive_backend.chat.domain.MessageBookmark;
import com.marchive.marchive_backend.chat.dto.ChatDtos.ChatDto;
import com.marchive.marchive_backend.chat.dto.ChatDtos.ChatListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.ChatWithMessagesDto;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageDto;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchNewChatResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchResponse;
import com.marchive.marchive_backend.chat.repository.ChatRepository;
import com.marchive.marchive_backend.chat.repository.MessageRepository;
import com.marchive.marchive_backend.chat.search.SearchEngine;
import com.marchive.marchive_backend.chat.search.SearchResult;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private static final int CHAT_PAGE_SIZE = 20;       // 채팅 목록 페이지 크기
    private static final int MESSAGE_PAGE_SIZE = 30;     // 메시지 목록 페이지 크기
    private static final int INITIAL_MESSAGE_SIZE = 20;  // 채팅 목록에 미리 담을 메시지 수
    private static final int MAX_BOOKMARKS = 3;          // assistant 메시지당 북마크 최대 3개

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SearchEngine searchEngine;
    private final ChatMapper chatMapper;

    public ChatService(
            ChatRepository chatRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            SearchEngine searchEngine,
            ChatMapper chatMapper
    ) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.searchEngine = searchEngine;
        this.chatMapper = chatMapper;
    }

    // 1. 채팅 목록 조회
    @Transactional(readOnly = true)
    public ChatListResponse getChatList(Long userId, Long cursor) {
        List<Chat> chats = chatRepository.findChatsByCursor(userId, cursor, PageRequest.of(0, CHAT_PAGE_SIZE));

        List<ChatDto> chatDtos = chats.stream()
                .map(chat -> new ChatDto(chat.getChatId(), chat.getTitle()))
                .toList();

        return new ChatListResponse(chatDtos);
    }

    // 2. 메시지 목록 조회
    @Transactional(readOnly = true)
    public MessageListResponse getMessageList(Long userId, Long chatId, Long cursor) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        validateOwner(chat, userId);

        List<Message> messages = messageRepository.findMessagesByCursor(
                chatId, cursor, PageRequest.of(0, MESSAGE_PAGE_SIZE));

        List<MessageDto> messageDtos = messages.stream()
                .map(chatMapper::toMessageDto)
                .toList();

        return new MessageListResponse(messageDtos);
    }

    // 3. 기존 채팅방에서 검색
    @Transactional
    public SearchResponse search(Long userId, Long chatId, String searchText) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
        validateOwner(chat, userId);

        // 사용자 질문 메시지 저장
        Message userMessage = new Message(chat, Message.Role.user, searchText);
        messageRepository.save(userMessage);

        // 검색 실행 → assistant 응답 메시지 저장
        Message assistantMessage = executeSearchAndSave(chat, searchText);

        return new SearchResponse(chatMapper.toMessageDto(assistantMessage));
    }

    // 4. 새 채팅방 생성 + 검색
    @Transactional
    public SearchNewChatResponse searchNewChat(Long userId, String searchText) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 새 채팅방 생성 (title은 검색어 그대로 — 나중에 LLM 요약으로 개선)
        Chat chat = new Chat(user, searchText);
        chatRepository.save(chat);

        // 사용자 질문 메시지 저장
        Message userMessage = new Message(chat, Message.Role.user, searchText);
        messageRepository.save(userMessage);

        // 검색 실행 → assistant 응답 메시지 저장
        Message assistantMessage = executeSearchAndSave(chat, searchText);

        // 응답: 새 채팅방(초기 메시지 포함) + 방금 생성된 assistant 메시지
        List<Message> initial = messageRepository.findRecentMessages(
                chat.getChatId(), PageRequest.of(0, INITIAL_MESSAGE_SIZE));
        List<MessageDto> initialDtos = initial.stream()
                .map(chatMapper::toMessageDto)
                .toList();

        ChatWithMessagesDto chatDto = new ChatWithMessagesDto(chat.getChatId(), chat.getTitle(), initialDtos);
        return new SearchNewChatResponse(chatDto, chatMapper.toMessageDto(assistantMessage));
    }

    // --- 공통 로직 ---

    // 검색 실행 후 assistant 메시지 + 북마크(최대 3개) 저장
    private Message executeSearchAndSave(Chat chat, String searchText) {
        SearchResult result = searchEngine.search(searchText);

        Message assistantMessage = new Message(chat, Message.Role.assistant, result.assistantContents());

        // 검색된 게시물을 최대 3개까지 북마크로 붙임
        List<Post> posts = result.matchedPosts();
        int limit = Math.min(posts.size(), MAX_BOOKMARKS);
        for (int i = 0; i < limit; i++) {
            assistantMessage.addBookmark(new MessageBookmark(assistantMessage, posts.get(i), i));
        }

        return messageRepository.save(assistantMessage);
    }

    // 채팅방 소유자 확인 (남의 채팅방 접근 차단)
    private void validateOwner(Chat chat, Long userId) {
        if (!chat.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("접근 권한이 없는 채팅방입니다.");
        }
    }
}
