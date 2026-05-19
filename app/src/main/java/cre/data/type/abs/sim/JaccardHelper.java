package cre.data.type.abs.sim;

import java.text.Normalizer;
import java.util.*;



public final class JaccardHelper extends StringComparator {


    private final CosineJaccardMode mode;
    private final int k; // Shingle-Größe (bei CHAR: k>=2 empfohlen; bei WORD: k>=1)
    int k2;

    public JaccardHelper(CosineJaccardMode mode, int k) {
        this.mode = Objects.requireNonNull(mode);
        this.k = Math.max(k, mode == CosineJaccardMode.CHAR ? 2 : 1);
        this.k2 = Math.max(k, mode == CosineJaccardMode.CHAR ? 2 : 1);
    }

    @Override
    public SimAlgorithm getAlgorithm() {
        return SimAlgorithm.JACC;
    }


    @Override
    public double compare(String a, String b) {
        String na = normalize(a);
        String nb = normalize(b);
        //Set<String> A = buildShingles(na);
        //Set<String> B = buildShingles(nb);
        //if (A.isEmpty() && B.isEmpty()) return 1.0;
        Set<String> A = new HashSet<>();
        Set<String> B = new HashSet<>();
        while(A.isEmpty() && B.isEmpty() && k2>0) {
            A = buildShingles(na);
            B = buildShingles(nb);
            k2--;
        }
        k2 = k;
        if (A.isEmpty() && B.isEmpty()) return 0.0;
        int inter = 0;
        Set<String> smaller = A.size() <= B.size() ? A : B;
        Set<String> larger  = A.size() >  B.size() ? A : B;
        for (String s : smaller) {
            if (larger.contains(s)) inter++;
        }
        int uni = A.size() + B.size() - inter;
        return uni == 0 ? 0.0 : (double) inter / (double) uni;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String n = s.toLowerCase(Locale.ROOT).trim();
        n = Normalizer.normalize(n, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        n = n.replace('&', ' ');
        n = n.replace('\u00A0', ' ');
        n = n.replaceAll("[^a-z0-9\\s]", " ");
        n = n.replaceAll("\\s+", " ").trim();
        return n;
    }

    private Set<String> buildShingles(String normalized) {
        Set<String> res = new HashSet<>();
        if (normalized.isEmpty()) return res;

        if (mode == CosineJaccardMode.CHAR) {
            String s = " " + normalized + " ";
            for (int i = 0; i <= s.length() - k2; i++) {
                res.add(s.substring(i, i + k2));
            }
        } else {
            String[] tok = normalized.split("\\s+");
            if (k2 == 1) {
                res.addAll(Arrays.asList(tok));
            } else {
                for (int i = 0; i <= tok.length - k2; i++) {
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < k2; j++) {
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

