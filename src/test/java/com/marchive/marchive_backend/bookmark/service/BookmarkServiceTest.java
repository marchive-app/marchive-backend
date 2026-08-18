package com.marchive.marchive_backend.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.bookmark.domain.Bookmark;
import com.marchive.marchive_backend.bookmark.domain.Post;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BookmarkItem;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkRequest;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.BulkResponse;
import com.marchive.marchive_backend.bookmark.dto.BookmarkDtos.MediaItem;
import com.marchive.marchive_backend.bookmark.repository.BookmarkRepository;
import com.marchive.marchive_backend.bookmark.repository.PostRepository;
import com.marchive.marchive_backend.global.s3.S3Service;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import com.marchive.marchive_backend.igaccount.service.IgAccountService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private BookmarkRepository bookmarkRepository;
    @Mock
    private IgAccountService igAccountService;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User createUserWithId(Long id) {
        User user = new User("google-sub", "test@gmail.com", "테스트");
        ReflectionTestUtils.setField(user, "userId", id);
        return user;
    }

    private IgAccount createIgAccountWithId(Long id, User owner, String igUserId, String handle) {
        IgAccount igAccount = new IgAccount(owner, igUserId, handle);
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
    void 연동된_계정으로_북마크를_저장한다() {
        User user = createUserWithId(1L);
        IgAccount igAccount = createIgAccountWithId(10L, user, "75464276161", "my_insta");
        when(igAccountService.getOrCreateAccount(1L, "75464276161", "my_insta")).thenReturn(igAccount);
        when(postRepository.findByIgCode(anyString())).thenReturn(java.util.Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("75464276161", "my_insta", List.of(createItem("code1")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void 연동되지_않은_계정이면_자동으로_생성되어_북마크가_저장된다() {
        User user = createUserWithId(1L);
        IgAccount newAccount = createIgAccountWithId(10L, user, "new_ig_id", "new_handle");
        when(igAccountService.getOrCreateAccount(1L, "new_ig_id", "new_handle"))
                .thenReturn(newAccount);
        when(postRepository.findByIgCode(anyString())).thenReturn(java.util.Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("new_ig_id", "new_handle", List.of(createItem("code1")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void 이미_존재하는_게시물이면_새로_저장하지_않고_재사용한다() {
        User user = createUserWithId(1L);
        IgAccount igAccount = createIgAccountWithId(10L, user, "75464276161", "my_insta");
        when(igAccountService.getOrCreateAccount(1L, "75464276161", "my_insta")).thenReturn(igAccount);

        Post existingPost = new Post("code1", "author", "caption", null, 5);
        when(postRepository.findByIgCode("code1")).thenReturn(java.util.Optional.of(existingPost));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("75464276161", "my_insta", List.of(createItem("code1")));
        bookmarkService.saveBulk(1L, request);

        verify(postRepository, never()).save(any(Post.class));
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    void 같은_계정이_같은_게시물을_이미_북마크했으면_중복_저장하지_않는다() {
        User user = createUserWithId(1L);
        IgAccount igAccount = createIgAccountWithId(10L, user, "75464276161", "my_insta");
        when(igAccountService.getOrCreateAccount(1L, "75464276161", "my_insta")).thenReturn(igAccount);

        Post existingPost = new Post("code1", "author", "caption", null, 5);
        when(postRepository.findByIgCode("code1")).thenReturn(java.util.Optional.of(existingPost));
        when(bookmarkRepository.existsByIgAccountAndPost(igAccount, existingPost)).thenReturn(true);

        BulkRequest request = new BulkRequest("75464276161", "my_insta", List.of(createItem("code1")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository, never()).save(any(Bookmark.class));
    }

    @Test
    void 여러_게시물을_한번에_저장하면_모두_처리된다() {
        User user = createUserWithId(1L);
        IgAccount igAccount = createIgAccountWithId(10L, user, "75464276161", "my_insta");
        when(igAccountService.getOrCreateAccount(1L, "75464276161", "my_insta")).thenReturn(igAccount);
        when(postRepository.findByIgCode(anyString())).thenReturn(java.util.Optional.empty());
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookmarkRepository.existsByIgAccountAndPost(any(), any())).thenReturn(false);

        BulkRequest request = new BulkRequest("75464276161", "my_insta",
                List.of(createItem("code1"), createItem("code2"), createItem("code3")));
        BulkResponse response = bookmarkService.saveBulk(1L, request);

        assertThat(response.success()).isTrue();
        verify(bookmarkRepository, times(3)).save(any(Bookmark.class));
    }
}