package com.dreamhouse.ai.cache.service.impl;

import com.dreamhouse.ai.cache.service.QueryKeyService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.StringJoiner;

@Service
public class QueryKeyServiceImpl implements QueryKeyService {

    /**
     * Builds a cache key from namespace, version, and additional parts.
     * @param namespace the namespace for the cache key
     * @param version the version number for the cache key
     * @param parts additional parts to include in the key
     * @return the constructed cache key string
     */
    @Override
    public String build(String namespace, int version, Object... parts) {
        StringJoiner joiner = new StringJoiner("|");
        for (Object p : parts) {
            if (p != null)
                joiner.add(p.toString().trim().toLowerCase());
            else
                joiner.add("null");
        }

        String base = joiner.toString();
        String hash = sha256Hex(base);

        return namespace + ":v" + version + ":" + hash;
    }


    /**
     * Builds a lock key from namespace, version, and additional parts.
     * @param namespace the namespace for the lock key
     * @param version the version number for the lock key
     * @param parts additional parts to include in the key
     * @return the constructed lock key string
     */
    @Override
    public String lockKey(String namespace, int version, Object... parts) {
        return "lock:" + build(namespace, version, parts);
    }


    @NotNull
    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(Objects.requireNonNull(input).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 12);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute SHA-256", e);
        }
    }
}
