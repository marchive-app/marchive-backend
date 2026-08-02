package com.marchive.marchive_backend.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.chat.domain.Chat;
import com.marchive.marchive_backend.chat.domain.Message;
import com.marchive.marchive_backend.chat.dto.ChatDtos.ChatListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.MessageListResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchNewChatResponse;
import com.marchive.marchive_backend.chat.dto.ChatDtos.SearchResponse;
import com.marchive.marchive_backend.chat.repository.ChatRepository;
import com.marchive.marchive_backend.chat.repository.MessageRepository;
import com.marchive.marchive_backend.chat.search.SearchEngine;
import com.marchive.marchive_backend.chat.search.SearchResult;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SearchEngine searchEngine;

    // ChatMapper는 순수 변환 로직이라 진짜 객체를 씀 (Mock 불필요)
    private final ChatMapper chatMapper = new ChatMapper();

    private ChatService chatService;

    // @InjectMocks 대신 직접 생성 (chatMapper를 실제 객체로 넣기 위해)
    private ChatService createService() {
        return new ChatService(chatRepository, messageRepository, userRepository, searchEngine, chatMapper);
    }

    // --- 헬퍼 ---
    private User createUserWithId(Long id) {
        User user = new User("google-sub", "test@gmail.com", "테스트");
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private Chat createChatWithId(Long chatId, User owner) {
        Chat chat = new Chat(owner, "테스트 채팅방");
        ReflectionTestUtils.setField(chat, "chatId", chatId);
        return chat;
    }

    private Message createMessageWithId(Long id, Chat chat, Message.Role role, String contents) {
        Message message = new Message(chat, role, contents);
        ReflectionTestUtils.setField(message, "messageId", id);
        return message;
    }

    @Test
    void 채팅_목록을_조회하면_제목만_담긴_목록을_반환한다() {
        chatService = createService();
        User user = createUserWithId(1L);
        Chat chat1 = createChatWithId(10L, user);
        Chat chat2 = createChatWithId(11L, user);
        when(chatRepository.findChatsByCursor(eq(1L), eq(0L), any(Pageable.class)))
                .thenReturn(List.of(chat2, chat1));

        ChatListResponse response = chatService.getChatList(1L, 0L);

        assertThat(response.chatList()).hasSize(2);
        assertThat(response.chatList().get(0).title()).isEqualTo("테스트 채팅방");
        // 채팅 목록 조회 시 메시지는 조회하지 않아야 함 (N+1 방지)
        verify(messageRepository, never()).findRecentMessages(anyLong(), any(Pageable.class));
    }

    @Test
    void 메시지_목록을_조회하면_최신순_메시지를_반환한다() {
        chatService = createService();
        User user = createUserWithId(1L);
        Chat chat = createChatWithId(10L, user);
        Message msg = createMessageWithId(100L, chat, Message.Role.assistant, "응답입니다");

        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(messageRepository.findMessagesByCursor(eq(10L), eq(0L), any(Pageable.class)))
                .thenReturn(List.of(msg));

        MessageListResponse response = chatService.getMessageList(1L, 10L, 0L);

        assertThat(response.messageList()).hasSize(1);
        assertThat(response.messageList().get(0).contents()).isEqualTo("응답입니다");
        assertThat(response.messageList().get(0).role()).isEqualTo("assistant");
    }

    @Test
    void 남의_채팅방에_접근하면_예외가_발생한다() {
        chatService = createService();
        User owner = createUserWithId(1L);
        Chat chat = createChatWithId(10L, owner);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));

        // userId=999(다른 사용자)로 접근 시도
        assertThatThrownBy(() -> chatService.getMessageList(999L, 10L, 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("권한");
    }

    @Test
    void 기존_채팅방에서_검색하면_질문과_응답_메시지가_저장된다() {
        chatService = createService();
        User user = createUserWithId(1L);
        Chat chat = createChatWithId(10L, user);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));
        when(searchEngine.search("커피"))
                .thenReturn(new SearchResult("검색 결과입니다", List.of()));
        // save 시 messageId 채워서 반환
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "messageId", 100L);
            return m;
        });

        SearchResponse response = chatService.search(1L, 10L, "커피");

        assertThat(response.message().role()).isEqualTo("assistant");
        assertThat(response.message().contents()).isEqualTo("검색 결과입니다");
        // 사용자 질문 + assistant 응답 = save 2번 호출
        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void 새_채팅방을_생성하면_채팅방과_응답을_반환한다() {
        chatService = createService();
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatRepository.save(any(Chat.class))).thenAnswer(inv -> {
            Chat c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "chatId", 10L);
            return c;
        });
        when(searchEngine.search("여행"))
                .thenReturn(new SearchResult("여행 검색 결과", List.of()));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "messageId", 100L);
            return m;
        });
        when(messageRepository.findRecentMessages(eq(10L), any(Pageable.class)))
                .thenReturn(List.of());

        SearchNewChatResponse response = chatService.searchNewChat(1L, "여행");

        assertThat(response.chat().id()).isEqualTo(10L);
        assertThat(response.chat().title()).isEqualTo("여행");  // title = 검색어
        assertThat(response.message().contents()).isEqualTo("여행 검색 결과");
    }

    @Test
    void 검색결과_게시물이_3개를_초과해도_최대_3개만_북마크된다() {
        chatService = createService();
        User user = createUserWithId(1L);
        Chat chat = createChatWithId(10L, user);
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));

        // 게시물 5개를 검색 결과로 반환
        List<Post> fivePosts = List.of(
                mock(Post.class), mock(Post.class), mock(Post.class),
                mock(Post.class), mock(Post.class));
        when(searchEngine.search("음식"))
                .thenReturn(new SearchResult("음식 결과", fivePosts));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "messageId", 100L);
            return m;
        });

        SearchResponse response = chatService.search(1L, 10L, "음식");

        // 5개 중 최대 3개만 북마크로 담겨야 함
        assertThat(response.message().bookmarkList()).hasSizeLessThanOrEqualTo(3);
    }
}