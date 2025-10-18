package com.dreamhouse.ai.llm.tool;

import com.dreamhouse.ai.llm.model.ImageSearchResult;
import com.dreamhouse.ai.llm.service.impl.ImageSimilaritySearchServiceImpl;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class ImageSearchTool {

    private static final int MAX_IMAGE_BYTES = 10 * 1024 * 1024; // 10MB cap
    private static final List<String> ALLOWED_MIME = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    private final ImageSimilaritySearchServiceImpl imageSimilaritySearchService;

    public ImageSearchTool(ImageSimilaritySearchServiceImpl imageSimilaritySearchService) {
        this.imageSimilaritySearchService = imageSimilaritySearchService;
    }

    @Tool("Find similar house ads given a base64 image of a property. Returns inferred JSON description and similar ads.")
    public Map<String, Object> searchSimilarByImage(
            String base64Image,
            String mime,
            Integer k,
            String cityHint,
            String typeHint,
            Integer bedsHint,
            Double priceHint
    ) {
        try {
            if (base64Image == null || base64Image.isBlank()) {
                return Map.of("error", "MISSING_IMAGE_DATA");
            }

            String raw = base64Image;
            String effectiveMime = mime;
            String suggestedFilename = "uploaded-image";

            if (base64Image.startsWith("data:") && base64Image.contains(";base64,")) {
                int semi = base64Image.indexOf(";base64,");
                effectiveMime = base64Image.substring(5, semi);
                raw = base64Image.substring(semi + ";base64,".length());
                suggestedFilename = "image-from-data-url";
            }

            if (effectiveMime == null || effectiveMime.isBlank()) {
                effectiveMime = "image/jpeg";
            }
            String finalEffectiveMime = effectiveMime;
            if (ALLOWED_MIME.stream().noneMatch(m -> m.equalsIgnoreCase(finalEffectiveMime))) {
                return Map.of("error", "UNSUPPORTED_MIME", "mime", effectiveMime);
            }

            byte[] bytes = Base64.getDecoder().decode(raw);
            if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) {
                return Map.of("error", "INVALID_IMAGE_SIZE", "bytes", bytes.length);
            }

            MultipartFile file = new BytesMultipartFile("file", suggestedFilename, effectiveMime, bytes);

            ImageSearchResult r = imageSimilaritySearchService.searchByImage(
                    file, k, cityHint, typeHint, bedsHint, priceHint
            );

            return Map.of(
                    "inferredDescription", r.inferredDescription(),
                    "appliedHints", Map.of(
                            "city", cityHint,
                            "type", typeHint,
                            "bedsHint", bedsHint,
                            "priceHint", priceHint
                    ),
                    "results", r.results()
            );

        } catch (IllegalArgumentException badBase64) {
            return Map.of("error", "INVALID_BASE64_IMAGE");
        } catch (Exception e) {
            return Map.of("error", "IMAGE_SEARCH_FAILED", "details", e.getMessage());
        }
    }


    static final class BytesMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        BytesMultipartFile(String name, String originalFilename, String contentType, byte[] bytes) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes != null ? bytes : new byte[0];
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes.clone(); }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (OutputStream os = Files.newOutputStream(dest.toPath())) {
                os.write(bytes);
            }
        }
    }
}
