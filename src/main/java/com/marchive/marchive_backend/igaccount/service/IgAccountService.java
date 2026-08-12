package com.marchive.marchive_backend.igaccount.service;

import com.marchive.marchive_backend.auth.domain.User;
import com.marchive.marchive_backend.auth.repository.UserRepository;
import com.marchive.marchive_backend.igaccount.domain.IgAccount;
import com.marchive.marchive_backend.igaccount.dto.IgAccountDtos.IgAccountDto;
import com.marchive.marchive_backend.igaccount.dto.IgAccountDtos.IgAccountListResponse;
import com.marchive.marchive_backend.igaccount.dto.IgAccountDtos.LinkResponse;
import com.marchive.marchive_backend.igaccount.repository.IgAccountRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IgAccountService {

    private final UserRepository userRepository;
    private final IgAccountRepository igAccountRepository;

    public IgAccountService(UserRepository userRepository, IgAccountRepository igAccountRepository) {
        this.userRepository = userRepository;
        this.igAccountRepository = igAccountRepository;
    }

    @Transactional
    public LinkResponse link(Long userId, String igUserId, String igHandle) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        IgAccount igAccount = igAccountRepository.findByIgUserId(igUserId)
                .map(existing -> {
                    validateOwner(existing, userId);
                    existing.updateHandle(igHandle);
                    return existing;
                })
                .orElseGet(() -> igAccountRepository.save(new IgAccount(user, igUserId, igHandle)));

        return new LinkResponse(true, toDto(igAccount));
    }

    @Transactional(readOnly = true)
    public IgAccountListResponse getMyAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<IgAccountDto> accounts = igAccountRepository.findByUser(user).stream()
                .map(this::toDto)
                .toList();

        return new IgAccountListResponse(accounts);
    }

    // ig_user_id로 조회 + 소유권 검증
    @Transactional(readOnly = true)
    public IgAccount getLinkedAccount(Long userId, String igUserId) {
        IgAccount igAccount = igAccountRepository.findByIgUserId(igUserId)
                .orElseThrow(() -> new IllegalArgumentException("연동되지 않은 인스타그램 계정입니다."));

        if (!igAccount.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 인스타그램 계정이 아닙니다.");
        }

        return igAccount;
    }

    private void validateOwner(IgAccount igAccount, Long userId) {
        if (!igAccount.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("이미 다른 사용자와 연동된 인스타그램 계정입니다.");
        }
    }

    private IgAccountDto toDto(IgAccount igAccount) {
        return new IgAccountDto(
                igAccount.getIgAccountId(),
                igAccount.getIgHandle()
        );
    }
}
