package com.marchive.marchive_backend.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.bookmark.domain.Bookmark;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BookmarkItem;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkRequest;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkResponse;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.MediaItem;
import com.marchive.marchive_backend.bookmark.repository.BookmarkRepository;
import com.marchive.marchive_backend.bookmark.repository.PostRepository;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import com.marchive.marchive_backend.igaccount.repository.IgAccountRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private IgAccountRepository igAccountRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User createUserWithId(Long id) {
        User user = new User("google-sub", "test@gmail.com", "테스트");
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private IgAccount createIgAccountWithId(Long id, User owner, String handle) {
        IgAccount igAccount = new IgAccount(owner, handle);
        ReflectionTestUtils.setField(igAccount, "igAccountId", id);
        return igAccount;
    }

    private BookmarkItem createItem(String igCode) {
        return new BookmarkItem(
                igCode, "author_handle_x", "캡션",
                OffsetDateTime.parse("2026-08-11T04:15:33Z"),
                10,
                List.of(new MediaItem("image", "https://cdn.example.com/1.jpg", 0)),
                "instagram"
        );
    }

    @Test
    void 처음_요청받은_ig_handle이면_인스타계정을_자동으로_생성한다() {
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(igAccountRepository.findByIgHandle("my_insta")).thenReturn(Optional.empty());
        IgAccount newAccount = createIgAccountWithId(10L, user, "my_insta");
        when(igAccountRepository.save(any(IgAccount.class))).thenReturn(newAccount);
        when(postRepository.findByIgCode(anyString())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("my_insta", List.of(createItem("code1")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(igAccountRepository).save(any(IgAccount.class));
    }

    @Test
    void 이미_등록된_ig_handle이면_기존_계정을_재사용한다() {
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        IgAccount existing = createIgAccountWithId(10L, user, "my_insta");
        when(igAccountRepository.findByIgHandle("my_insta")).thenReturn(Optional.of(existing));
        when(postRepository.findByIgCode(anyString())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("my_insta", List.of(createItem("code1")));
        bookmarkService.saveBulk(1L, request);

        verify(igAccountRepository, never()).save(any(IgAccount.class));
    }

    @Test
    void 이미_존재하는_게시물이면_새로_저장하지_않고_재사용한다() {
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        IgAccount igAccount = createIgAccountWithId(10L, user, "my_insta");
        when(igAccountRepository.findByIgHandle("my_insta")).thenReturn(Optional.of(igAccount));

        Post existingPost = new Post("code1", "author", "caption", null, 5);
        when(postRepository.findByIgCode("code1")).thenReturn(Optional.of(existingPost));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("my_insta", List.of(createItem("code1")));
        bookmarkService.saveBulk(1L, request);

        verify(postRepository, never()).save(any(Post.class));
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void 같은_계정이_같은_게시물을_이미_북마크했으면_중복_저장하지_않는다() {
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        IgAccount igAccount = createIgAccountWithId(10L, user, "my_insta");
        when(igAccountRepository.findByIgHandle("my_insta")).thenReturn(Optional.of(igAccount));

        Post existingPost = new Post("code1", "author", "caption", null, 5);
        when(postRepository.findByIgCode("code1")).thenReturn(Optional.of(existingPost));
        // 이미 북마크되어 있음
        when(bookmarkRepository.existsByIgAccountAndPost(igAccount, existingPost)).thenReturn(true);

        BulkRequest request = new BulkRequest("my_insta", List.of(createItem("code1")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void 여러_게시물을_한번에_저장하면_모두_처리된다() {
        User user = createUserWithId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        IgAccount igAccount = createIgAccountWithId(10L, user, "my_insta");
        when(igAccountRepository.findByIgHandle("my_insta")).thenReturn(Optional.of(igAccount));
        when(postRepository.findByIgCode(anyString())).thenReturn(Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("my_insta",
                List.of(createItem("code1"), createItem("code2"), createItem("code3")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository, times(3)).save(any(Bookmark.class));
    }

    @Test
    void 존재하지_않는_사용자면_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BulkRequest request = new BulkRequest("my_insta", List.of(createItem("code1")));

        assertThatThrownBy(() -> bookmarkService.saveBulk(999L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}