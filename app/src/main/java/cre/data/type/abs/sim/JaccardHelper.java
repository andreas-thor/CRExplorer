package cre.data.type.abs.sim;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class JaccardHelper extends StringComparator {

    private final CosineJaccardMode mode;
    private final int k;

    // Cache: Originalstring -> Shingles
    private final Map<String, Set<String>> shingleCache = new ConcurrentHashMap<>();

    public JaccardHelper(CosineJaccardMode mode, int k) {
        this.mode = Objects.requireNonNull(mode);
        this.k = Math.max(k, mode == CosineJaccardMode.CHAR ? 2 : 1);
    }

    @Override
    public SimAlgorithm getAlgorithm() {
        return SimAlgorithm.JACC;
    }

    @Override
    public double compare(String a, String b) {
        Set<String> A = getShingles(a);
        Set<String> B = getShingles(b);
        if (A.isEmpty() && B.isEmpty()) {
            return 0.0;
        }
        int intersection = 0;
        Set<String> smaller = A.size() <= B.size() ? A : B;
        Set<String> larger  = A.size() >  B.size() ? A : B;
        for (String s : smaller) {
            if (larger.contains(s)) {
                intersection++;
            }
        }
        int union = A.size() + B.size() - intersection;
        return union == 0 ? 0.0 : (double) intersection / union;
    }

    private Set<String> getShingles(String input) {
        return shingleCache.computeIfAbsent(input, s -> {
            String normalized = normalize(s);
            int currentK = k;
            Set<String> shingles = Collections.emptySet();
            while (shingles.isEmpty() && currentK > 0) {
                shingles = buildShingles(normalized, currentK);
                if (!shingles.isEmpty()) {
                    break;
                }
                currentK--;
            }
            return shingles;
        });
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        String n = s.toLowerCase(Locale.ROOT).trim();
        n = Normalizer.normalize(n, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.replace('&', ' ');
        n = n.replace('\u00A0', ' ');
        n = n.replaceAll("[^a-z0-9\\s]", " ");
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }

    private Set<String> buildShingles(String normalized, int shingleSize) {
        Set<String> result = new HashSet<>();
        if (normalized.isEmpty()) {
            return result;
        }
        if (mode == CosineJaccardMode.CHAR) {
            String s = " " + normalized + " ";
            if (s.length() < shingleSize) {
                return result;
            }
            for (int i = 0; i <= s.length() - shingleSize; i++) {
                result.add(s.substring(i, i + shingleSize));
            }
        } else {
            String[] tok = normalized.split("\\s+");
            if (shingleSize == 1) {
                result.addAll(Arrays.asList(tok));
                return result;
            }
            if (tok.length < shingleSize) {
                return result;
            }
            for (int i = 0; i <= tok.length - shingleSize; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < shingleSize; j++) {
                    if (j > 0) {
                        sb.append(' ');
                    }
                    sb.append(tok[i + j]);
                }
                result.add(sb.toString());
            }
        }
        return result;
    }
}