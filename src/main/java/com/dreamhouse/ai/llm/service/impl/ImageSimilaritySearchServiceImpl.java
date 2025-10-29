package com.dreamhouse.ai.llm.service.impl;

import com.dreamhouse.ai.house.dto.HouseAdDTO;
import com.dreamhouse.ai.house.model.entity.HouseAdEntity;
import com.dreamhouse.ai.llm.model.reply.ImageSearchReply;
import com.dreamhouse.ai.llm.service.ImageSimilaritySearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import net.coobird.thumbnailator.Thumbnails;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

@Service
public class ImageSimilaritySearchServiceImpl implements ImageSimilaritySearchService {
    private final ChatLanguageModel visionChatModel;
    private final EmbeddingModel embeddingModel;
    private final ModelMapper mapper;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public ImageSimilaritySearchServiceImpl(ChatLanguageModel visionChatModel, EmbeddingModel embeddingModel, ModelMapper mapper, ObjectMapper objectMapper) {
        this.visionChatModel = visionChatModel;
        this.embeddingModel = embeddingModel;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Searches for similar house advertisements using an uploaded image.
     * @param file the image file to analyze
     * @param k the number of similar results to return
     * @param cityHint optional city filter for results
     * @param typeHint optional property type filter for results
     * @param bedsHint optional bedroom count filter for results
     * @param priceHint optional price range filter for results
     * @return ImageSearchReply containing inferred description and similar house ads
     */
    @Override
    public ImageSearchReply searchByImage(MultipartFile file,
                                          Integer k,
                                          String cityHint,
                                          String typeHint,
                                          Integer bedsHint,
                                          Double priceHint) {
        try {
            byte[] bytes = file.getBytes();
            String mime  = file.getContentType() != null ? file.getContentType() : "image/jpeg";

            if (!mime.startsWith("image/")) {
                throw new IllegalArgumentException("Unsupported file type: " + mime);
            }

            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img.getWidth() > 1024 || img.getHeight() > 1024) {
                BufferedImage resized = Thumbnails.of(img)
                        .size(1024, 1024)
                        .outputQuality(0.8)
                        .asBufferedImage();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resized, mime.split("/")[1], baos);
                bytes = baos.toByteArray();
            }


            String prompt = """
                Analyze this property photo. Return compact JSON with keys:
                "style","exterior","stories","bed_bath_hint","features","condition","notes".
                Keep it factual; do not guess location.
                """;

            String dataUrl = "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
            String structured = visionChatModel.generate(
                    UserMessage.from(
                            TextContent.from(prompt),
                            ImageContent.from(Image.builder().url(dataUrl).build())
                    )
            ).content().text();

            objectMapper.readTree(structured);

            String embeddingText = "Property image summary: " + structured
                    + (typeHint != null && !typeHint.isBlank() ? (" | type=" + typeHint) : "")
                    + (cityHint != null && !cityHint.isBlank() ? (" | city=" + cityHint) : "")
                    + (bedsHint != null ? (" | beds=" + bedsHint) : "")
                    + (priceHint != null ? (" | price=" + priceHint) : "");

            var emb = embeddingModel.embed(embeddingText).content();
            float[] vec = emb.vector();


            List<HouseAdEntity> entities = similarByVector(vec, k != null ? k : 12, cityHint, typeHint, bedsHint, priceHint);

            var houseAds = entities.stream()
                    .map(e -> mapper.map(e, HouseAdDTO.class))
                    .toList();

            return new ImageSearchReply(structured, vec, houseAds);

        } catch (Exception e) {
            throw new RuntimeException("Image similarity search failed", e);
        }
        }

    /**
     * Searches for similar house advertisements using vector similarity.
     * @param query the query vector for similarity search
     * @param k the number of similar results to return
     * @param cityHint optional city filter for results
     * @param typeHint optional property type filter for results
     * @param anchorBeds optional bedroom count filter for results
     * @param anchorPrice optional price range filter for results
     * @return List of HouseAdEntity containing similar house advertisements
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<HouseAdEntity> similarByVector(float[] query,
                                               int k,
                                               String cityHint,
                                               String typeHint,
                                               Integer anchorBeds,
                                               Double  anchorPrice) {
        StringBuilder sql = new StringBuilder("""
        SELECT * FROM house_ads
        WHERE embedding IS NOT NULL
        """);

        if (cityHint != null && !cityHint.isBlank()) {
            sql.append(" AND LOWER(city) = LOWER(:city) ");
        }
        if (typeHint != null && !typeHint.isBlank()) {
            sql.append(" AND LOWER(type) = LOWER(:type) ");
        }
        if (anchorBeds != null) {
            sql.append(" AND beds BETWEEN :minBeds AND :maxBeds ");
        }
        if (anchorPrice != null) {
            sql.append(" AND price BETWEEN :minPrice AND :maxPrice ");
        }

        sql.append("""
        ORDER BY embedding <-> :query
        LIMIT :k
        """);

        var q = em.createNativeQuery(sql.toString(), HouseAdEntity.class)
                .setParameter("query", query)
                .setParameter("k", k);

        if (cityHint != null && !cityHint.isBlank()) q.setParameter("city", cityHint);
        if (typeHint != null && !typeHint.isBlank()) q.setParameter("type", typeHint);

        if (anchorBeds != null) {
            q.setParameter("minBeds", Math.max(0, anchorBeds - 1));
            q.setParameter("maxBeds", anchorBeds + 1);
        }
        if (anchorPrice != null) {
            double minPrice = anchorPrice * 0.85;
            double maxPrice = anchorPrice * 1.15;
            q.setParameter("minPrice", minPrice);
            q.setParameter("maxPrice", maxPrice);
        }

        return q.getResultList();
    }
}
