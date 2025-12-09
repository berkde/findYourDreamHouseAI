package com.dreamhouse.ai.llm.configuration.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import org.jetbrains.annotations.NotNull;
import org.nibor.autolink.LinkExtractor;
import org.nibor.autolink.LinkSpan;
import org.nibor.autolink.LinkType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Locale;

/**
 * Lightweight URL sanitizer guardrail.
 * - Does NOT perform any network calls / reachability checks.
 * - Only removes obviously unsafe URL schemes (javascript:, data:, file:, vbscript:, etc.).
 * - Leaves normal http/https URLs (e.g., presigned S3 links) untouched.
 * - If a change is made, returns successWith(modifiedText); otherwise, simple success().
 */
@Component
public class UrlCheckerOutputGuardrail implements OutputGuardrail {

    private static final String REMOVED_PLACEHOLDER = "[removed-unsafe-url]";

    private final LinkExtractor linkExtractor = LinkExtractor.builder()
            .linkTypes(EnumSet.of(LinkType.URL))
            .build();

    @Override
    public OutputGuardrailResult validate(@NotNull OutputGuardrailRequest request) {
        AiMessage aiMessage = request.responseFromLLM().aiMessage();
        if (aiMessage == null || aiMessage.text() == null) {
            return success();
        }

        String originalText = aiMessage.text();
        String trimmed = originalText.trim();
        if (trimmed.isEmpty()) {
            return success();
        }

        String sanitized = sanitizeUrls(originalText);

        if (sanitized.equals(originalText)) {
            return success();
        }

        return successWith(sanitized);
    }


    @NotNull
    private String sanitizeUrls(@NotNull String text) {
        StringBuilder out = new StringBuilder();
        int lastIndex = 0;

        Iterable<LinkSpan> spans = linkExtractor.extractLinks(text);

        for (LinkSpan span : spans) {
            int start = span.getBeginIndex();
            int end = span.getEndIndex();

            if (start < lastIndex) {
                continue;
            }

            out.append(text, lastIndex, start);

            String url = text.substring(start, end);

            if (isClearlyUnsafeUrl(url)) {
                out.append(REMOVED_PLACEHOLDER);
            } else {
                out.append(url);
            }

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            out.append(text.substring(lastIndex));
        }

        return new String(out);
    }


    private boolean isClearlyUnsafeUrl(@NotNull String url) {
        String lowerCasesUrl = url.trim().toLowerCase(Locale.ROOT);

        while (lowerCasesUrl.startsWith("<") || lowerCasesUrl.startsWith("(")) {
            lowerCasesUrl = lowerCasesUrl.substring(1).trim();
        }

        return lowerCasesUrl.startsWith("javascript:")
                || lowerCasesUrl.startsWith("data:")
                || lowerCasesUrl.startsWith("file:")
                || lowerCasesUrl.startsWith("vbscript:")
                || lowerCasesUrl.startsWith("ms-excel:")
                || lowerCasesUrl.startsWith("ms-word:")
                || lowerCasesUrl.startsWith("ms-powerpoint:");
    }
}
