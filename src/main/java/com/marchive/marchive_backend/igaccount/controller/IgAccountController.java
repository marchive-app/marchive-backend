package com.marchive.marchive_backend.igaccount.controller;

import com.marchive.marchive_backend.igaccount.dto.IgAccountDtos.LinkRequest;
import com.marchive.marchive_backend.igaccount.dto.IgAccountDtos.LinkResponse;
import com.marchive.marchive_backend.igaccount.service.IgAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IgAccountController {

    private IgAccountService igAccountService;

    @PostMapping
    public ResponseEntity<LinkResponse> link(
            @AuthenticationPrincipal Long userId,
            @RequestBody LinkRequest request
    ) {
        return ResponseEntity.ok(igAccountService.link(userId, request.igUserId(), request.igHandle()));
    }
}
