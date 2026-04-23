package dev.smo.shortener.backend.urlservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PageResponse<T>(
        List<T> content,

        @JsonProperty("number")
        int page,

        int size,

        long totalElements,
        int totalPages,

        boolean first,
        boolean last,

        int numberOfElements,
        boolean empty
) {}