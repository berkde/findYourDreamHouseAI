package com.dreamhouse.ai.llm.model.request;

import com.dreamhouse.ai.llm.exception.BadRequestException;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public record ChatRequest(
        @JsonProperty("query")
        @NotBlank(message = "Query must not be blank")
        @Size(max = 2000, message = "Query must not exceed 2000 characters")
        String query
) implements SearchRequest {
    private static final Pattern ALLOWED =
            Pattern.compile("^[\\p{L}\\p{N}\\p{P}\\p{Zs}\\p{S}\\r\\n\\t]+$");
    private static final Pattern DISALLOWED_HTML =
            Pattern.compile("(?i)<\\s*(script|style|iframe|object|embed|link|meta)\\b");

    public ChatRequest {
        Objects.requireNonNull(query, "Query is required");

        query = Normalizer.normalize(query, Normalizer.Form.NFC).strip();

        if (query.isEmpty()) {
            throw new BadRequestException("Query must not be empty or blank");
        }

        if (query.length() > 2000) {
            throw new BadRequestException("Query must not exceed 2000 characters");
        }

        int codePoints = query.codePointCount(0, query.length());
        if (codePoints > 2000) {
            throw new BadRequestException("Query must not exceed 2000 Unicode characters");
        }

        for (int i = 0; i < query.length(); ) {
            int cp = query.codePointAt(i);
            int type = Character.getType(cp);
            if ((type == Character.CONTROL || type == Character.FORMAT) && !Character.isWhitespace(cp)) {
                throw new BadRequestException("Query contains invalid control/format characters");
            }
            i += Character.charCount(cp);
        }

        if (!ALLOWED.matcher(query).matches()) {
            throw new BadRequestException("Query contains unsupported symbols");
        }

        if (DISALLOWED_HTML.matcher(query).find()) {
            throw new BadRequestException("Query contains disallowed HTML tags");
        }

        String ql = query.toLowerCase();
        if (ql.contains("system prompt") || ql.contains("ignore previous instructions")) {
            throw new BadRequestException("Unsafe or disallowed content in query");
        }

        String[] words = query.split("\\s+");
        if (words.length > 10) {
            long unique = Arrays.stream(words).map(String::toLowerCase).distinct().count();
            double repetitionRatio = 1.0 - (double) unique / words.length;
            if (repetitionRatio > 0.6) {
                throw new BadRequestException("Query appears overly repetitive");
            }
        }

        long newlines = query.chars().filter(ch -> ch == '\n').count();
        if (newlines > 10) {
            throw new BadRequestException("Query contains too many line breaks");
        }
    }

}
