package cre.data.type.abs.sim;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;


public class CosineHelper extends StringComparator {

    private final CosineJaccardMode mode;
    private final int n; // n-Gramm-Größe (für WORD: Wort-n-Gramme/Shingles, für CHAR: Zeichen-n-Gramme)

    public CosineHelper(CosineJaccardMode mode, int n) {
        if (n < 1) throw new IllegalArgumentException("n must be >= 1");
        this.mode = Objects.requireNonNull(mode);
        this.n = n;
    }

    @Override
    public SimAlgorithm getAlgorithm() {
        return SimAlgorithm.COS;
    }


    @Override
    public double compare(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        Map<String, Integer> v1 = tfVector(s1);
        Map<String, Integer> v2 = tfVector(s2);

        if (v1.isEmpty() && v2.isEmpty()) return 1.0;
        if (v1.isEmpty() || v2.isEmpty()) return 0.0;

        Map<String, Integer> a = v1.size() <= v2.size() ? v1 : v2;
        Map<String, Integer> b = (a == v1) ? v2 : v1;

        double dot = 0.0;
        for (Map.Entry<String, Integer> e : a.entrySet()) {
            Integer bv = b.get(e.getKey());
            if (bv != null) dot += e.getValue() * bv;
        }

        double n1 = 0.0, n2 = 0.0;
        for (int x : v1.values()) n1 += x * (double) x;
        for (int y : v2.values()) n2 += y * (double) y;

        if (n1 == 0.0 || n2 == 0.0) return 0.0;
        double cos = dot / (Math.sqrt(n1) * Math.sqrt(n2));

        if (cos < 0) cos = 0;
        if (cos > 1) cos = 1;
        return cos;
    }

    private Map<String, Integer> tfVector(String text) {
        return switch (mode) {
            case CosineJaccardMode.WORD -> tfShingles(wordTokens(text), n);
            case CosineJaccardMode.CHAR -> tfCharNGrams(normalize(text), n);
        };
    }

    private static final Pattern SPLIT = Pattern.compile("\\p{Punct}|\\s+");

    private static String normalize(String s) {
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return noAccent.toLowerCase(Locale.ROOT).trim();
    }

    private static List<String> wordTokens(String s) {
        String n = normalize(s);
        List<String> out = new ArrayList<>();
        for (String t : SPLIT.split(n)) {
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }

    private static Map<String, Integer> tfShingles(List<String> tokens, int k) {
        Map<String, Integer> tf = new HashMap<>();
        if (tokens.isEmpty()) return tf;
        if (k <= 1) {
            for (String t : tokens) tf.merge(t, 1, Integer::sum);
            return tf;
        }
        for (int i = 0; i + k <= tokens.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < k; j++) {
                if (j > 0) sb.append('\u0001');
                sb.append(tokens.get(i + j));
            }
            tf.merge(sb.toString(), 1, Integer::sum);
        }
        return tf;
    }

    private static Map<String, Integer> tfCharNGrams(String s, int k) {
        Map<String, Integer> tf = new HashMap<>();
        if (s.isEmpty()) return tf;
        String padded = " " + s + " ";
        for (int i = 0; i + k <= padded.length(); i++) {
            String gram = padded.substring(i, i + k);
            tf.merge(gram, 1, Integer::sum);
        }
        return tf;
    }
}
