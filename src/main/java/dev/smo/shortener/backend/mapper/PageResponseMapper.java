package dev.smo.shortener.backend.mapper;

import dev.smo.shortener.backend.api.ResponseUrl;
import dev.smo.shortener.backend.urlservice.PageResponse;
import dev.smo.shortener.backend.urlservice.UrlResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageResponseMapper {

    private final ResponseUrlMapper responseUrlMapper;

    public PageResponseMapper(ResponseUrlMapper responseUrlMapper) {
        this.responseUrlMapper = responseUrlMapper;
    }

    public PageResponse<ResponseUrl> toResponse(PageResponse<UrlResponse> source) {

        List<ResponseUrl> content = source.content()
                .stream()
                .map(responseUrlMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                source.page(),
                source.size(),
                source.totalElements(),
                source.totalPages(),
                source.first(),
                source.last(),
                source.numberOfElements(),
                source.empty()
        );
    }
}