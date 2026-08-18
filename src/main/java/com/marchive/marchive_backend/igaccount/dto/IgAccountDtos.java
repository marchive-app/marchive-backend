package com.marchive.marchive_backend.igaccount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class IgAccountDtos {

    public record LinkRequest(
            @JsonProperty("ig_user_id") String igUserId,
            @JsonProperty("ig_handle") String igHandle
    ) {
    }

    public record IgAccountDto(
            Long id,
            @JsonProperty("ig_handle") String igHandle
    ) {
    }

    public record LinkResponse(boolean success, IgAccountDto igAccount) {
    }

    public record IgAccountListResponse(List<IgAccountDto> igAccounts) {
    }
}
