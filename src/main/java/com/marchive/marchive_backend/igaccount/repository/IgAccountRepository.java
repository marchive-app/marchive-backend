package com.marchive.marchive_backend.igaccount.repository;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IgAccountRepository extends JpaRepository<IgAccount, Long> {

    List<IgAccount> findByUser(User user);

    Optional<IgAccount> findByIgHandle(String igHandle);
}
