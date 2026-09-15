import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ataque à Cifra de Vigenère: descobre o tamanho da chave (Kasiski + índice de coincidência) e depois cada letra da chave (análise de frequência,
 * qui-quadrado contra a distribuição de letras do português).
 */
public class VigenereBreaker {

    private static final int MAX_KEY_LENGTH = 20;

    // Frequência relativa (%) das letras no português, sem acentos.
    private static final double[] PT_FREQ = {
            14.63, 1.04, 3.88, 4.99, 12.57, 1.02, 1.30, 1.28, 6.18, 0.40,
            0.02, 2.78, 4.74, 5.05, 10.73, 2.52, 1.20, 6.53, 7.81, 4.34,
            4.63, 1.67, 0.01, 0.21, 0.01, 0.47
    };

    public static void run(String path) throws IOException {
        String cipherText = Files.readString(Path.of(path));

        String letters = onlyLetters(cipherText).toUpperCase();
        System.out.println("Letras no texto cifrado: " + letters.length());

        List<Integer> kasiskiCandidates = kasiskiExamination(letters);
        System.out.println("Candidatos (Kasiski, por frequência de fatores): " + kasiskiCandidates);

        System.out.println("Índice de coincidência médio por tamanho de chave:");
        for (int len = 1; len <= MAX_KEY_LENGTH; len++) {
            System.out.printf("  tamanho %2d: IC médio = %.4f%n", len, averageIndexOfCoincidence(letters, len));
        }

        int keyLength = bestKeyLengthByChiSquared(letters, kasiskiCandidates);
        System.out.println("Tamanho de chave escolhido: " + keyLength);

        String key = recoverKey(letters, keyLength);
        System.out.println("Chave recuperada: " + key);

        String plainText = VigenereCipher.decrypt(cipherText, key);
        System.out.println();
        System.out.println("--- Texto decifrado ---");
        System.out.println(plainText);

        boolean crib = plainText.toLowerCase().contains("seguranca");
        System.out.println();
        System.out.println("Contém \"seguranca\"? " + crib);
    }
    // --- Apenas letras, maiúsculas, sem acentos ---
    private static String onlyLetters(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // --- Kasiski: acha repetições de trigramas, fatora as distâncias entre elas ---

    private static List<Integer> kasiskiExamination(String letters) {
        Map<String, List<Integer>> positions = new HashMap<>();
        for (int i = 0; i + 3 <= letters.length(); i++) {
            String trigram = letters.substring(i, i + 3);
            positions.computeIfAbsent(trigram, k -> new ArrayList<>()).add(i);
        }

        Map<Integer, Integer> factorCount = new HashMap<>();
        for (List<Integer> occurrences : positions.values()) {
            if (occurrences.size() < 2) {
                continue;
            }
            for (int i = 1; i < occurrences.size(); i++) {
                int distance = occurrences.get(i) - occurrences.get(0);
                for (int factor = 2; factor <= MAX_KEY_LENGTH; factor++) {
                    if (distance % factor == 0) {
                        factorCount.merge(factor, 1, Integer::sum);
                    }
                }
            }
        }

        List<Integer> candidates = new ArrayList<>(factorCount.keySet());
        candidates.sort((a, b) -> factorCount.get(b) - factorCount.get(a));
        return candidates.subList(0, Math.min(5, candidates.size()));
    }

    // --- Índice de coincidência: mede o quão "não uniforme" é a distribuição de letras ---

    private static double indexOfCoincidence(String text) {
        int[] counts = new int[26];
        for (char c : text.toCharArray()) {
            counts[c - 'A']++;
        }
        int n = text.length();
        if (n < 2) {
            return 0.0;
        }
        long sum = 0;
        for (int count : counts) {
            sum += (long) count * (count - 1);
        }
        return (double) sum / ((long) n * (n - 1));
    }

    // --- Para cada tamanho candidato, calcula o índice de coincidência médio das colunas ---
    private static double averageIndexOfCoincidence(String letters, int keyLength) {
        double total = 0.0;
        for (int col = 0; col < keyLength; col++) {
            StringBuilder column = new StringBuilder();
            for (int i = col; i < letters.length(); i += keyLength) {
                column.append(letters.charAt(i));
            }
            total += indexOfCoincidence(column.toString());
        }
        return total / keyLength;
    }

    // --- Para cada tamanho candidato, monta a chave por qui-quadrado e mede o ajuste total ---

    private static int bestKeyLengthByChiSquared(String letters, List<Integer> kasiskiCandidates) {
        List<Integer> lengths = new ArrayList<>(kasiskiCandidates);
        for (int len = 1; len <= MAX_KEY_LENGTH; len++) {
            if (!lengths.contains(len)) {
                lengths.add(len);
            }
        }

        int bestLength = 1;
        double bestChiSquared = Double.MAX_VALUE;

        for (int len : lengths) {
            String key = recoverKey(letters, len);
            double chiSquared = totalChiSquared(letters, key);
            if (chiSquared < bestChiSquared) {
                bestChiSquared = chiSquared;
                bestLength = len;
            }
        }
        return bestLength;
    }

    // --- Calcula o qui-quadrado total do texto decifrado com a chave fornecida, contra a distribuição de letras do português ---
    private static double totalChiSquared(String cipherLetters, String key) {
        String decrypted = VigenereCipher.decrypt(cipherLetters, key).toUpperCase();
        int[] counts = new int[26];
        for (char c : decrypted.toCharArray()) {
            counts[c - 'A']++;
        }
        int n = decrypted.length();
        double chiSquared = 0.0;
        for (int i = 0; i < 26; i++) {
            double expected = PT_FREQ[i] / 100.0 * n;
            double diff = counts[i] - expected;
            chiSquared += (diff * diff) / expected;
        }
        return chiSquared;
    }

    // --- Recupera a chave: uma letra por coluna, via qui-quadrado contra o português ---

    private static String recoverKey(String letters, int keyLength) {
        StringBuilder key = new StringBuilder();
        for (int col = 0; col < keyLength; col++) {
            StringBuilder column = new StringBuilder();
            for (int i = col; i < letters.length(); i += keyLength) {
                column.append(letters.charAt(i));
            }
            key.append(bestShiftForColumn(column.toString()));
        }
        return key.toString();
    }

    // --- Para uma coluna, encontra a letra da chave que minimiza o qui-quadrado contra o português ---
    private static char bestShiftForColumn(String column) {
        int bestShift = 0;
        double bestChiSquared = Double.MAX_VALUE;

        for (int shift = 0; shift < 26; shift++) {
            int[] counts = new int[26];
            for (char c : column.toCharArray()) {
                int shifted = (c - 'A' - shift + 26) % 26;
                counts[shifted]++;
            }
            int n = column.length();
            double chiSquared = 0.0;
            for (int i = 0; i < 26; i++) {
                double expected = PT_FREQ[i] / 100.0 * n;
                double diff = counts[i] - expected;
                chiSquared += (diff * diff) / expected;
            }
            if (chiSquared < bestChiSquared) {
                bestChiSquared = chiSquared;
                bestShift = shift;
            }
        }
        return (char) ('A' + bestShift);
    }
}
