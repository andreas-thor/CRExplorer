package cre.data.type.abs;

import java.text.Normalizer;
import java.util.*;

public final class JaccardHelper {

    public enum Mode { CHAR, WORD }

    private final Mode mode;
    private final int k; // Shingle-Größe (bei CHAR: k>=2 empfohlen; bei WORD: k>=1)

    public JaccardHelper() {
        this(Mode.CHAR, 3);
    }

    public JaccardHelper(Mode mode, int k) {
        this.mode = Objects.requireNonNull(mode);
        this.k = Math.max(k, mode == Mode.CHAR ? 2 : 1);
    }

    /** Vergleicht zwei Strings per Jaccard-Ähnlichkeit (0..1). Nulls/Leerstrings werden sauber behandelt. */
    public double compare(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        Set<String> A = buildShingles(na);
        Set<String> B = buildShingles(nb);
        if (A.isEmpty() && B.isEmpty()) return 1.0;

        // |A ∩ B|
        int inter = 0;
        Set<String> smaller = A.size() <= B.size() ? A : B;
        Set<String> larger  = A.size() >  B.size() ? A : B;
        for (String s : smaller) {
            if (larger.contains(s)) inter++;
        }
        int uni = A.size() + B.size() - inter;
        return uni == 0 ? 0.0 : (double) inter / (double) uni;
    }

    // --- Helpers ---

    private String normalize(String s) {
        if (s == null) return "";
        String n = s.toLowerCase(Locale.ROOT).trim();
        n = Normalizer.normalize(n, Normalizer.Form.NFD).replaceAll("\\p{M}", ""); // Diakritika raus
        n = n.replace('&', ' ');
        n = n.replace('\u00A0', ' ');        // NBSP -> Space
        n = n.replaceAll("[^a-z0-9\\s]", " "); // nur a-z0-9 und Whitespace behalten
        n = n.replaceAll("\\s+", " ").trim(); // Whitespace kollabieren
        return n;
    }

    private Set<String> buildShingles(String normalized) {
        Set<String> res = new HashSet<>();
        if (normalized.isEmpty()) return res;

        if (mode == Mode.CHAR) {
            String s = " " + normalized + " ";        // leichte Randrobustheit
            for (int i = 0; i <= s.length() - k; i++) {
                res.add(s.substring(i, i + k));
            }
        } else {
            String[] tok = normalized.split("\\s+");
            if (k == 1) {
                res.addAll(Arrays.asList(tok));
            } else {
                for (int i = 0; i <= tok.length - k; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < k; j++) {
                        if (j > 0) sb.append(' ');
                        sb.append(tok[i + j]);
                    }
                    res.add(sb.toString());
                }
            }
        }
        return res;
    }
}

