package com.marchive.marchive_backend.bookmark.repository;

import com.marchive.marchive_backend.bookmark.domain.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    Optional<Post> findByIgCode(String igCode);
}
