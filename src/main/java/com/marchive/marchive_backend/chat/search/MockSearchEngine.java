package com.marchive.marchive_backend.chat.search;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockSearchEngine implements SearchEngine {

    @Override
    public SearchResult search(String searchText, Long igAccountId) {
        // 지금은 항상 성공
        String contents = "'" + searchText + "'에 대한 검색 결과입니다. (임시 응답)";
        // 실제로는 여기서 벡터 검색 → 매칭된 Post 최대 3개 반환
        return SearchResult.success(contents, List.of());
    }
}
