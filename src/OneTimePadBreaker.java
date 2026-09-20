import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Ataque de "many-time pad": quando a mesma chave de One-Time Pad é reutilizada em várias
 * mensagens, XOR(C1, C2) = XOR(P1, P2) — a chave desaparece e sobra só a relação entre os
 * textos originais. Para cada posição, testamos os 256 valores possíveis de byte de chave e
 * escolhemos o único que torna TODAS as mensagens disponíveis, naquela posição, letras ou
 * espaço ao mesmo tempo. Letra XOR letra raramente resulta em outra letra, então uma
 * concordância unânime entre 3+ mensagens praticamente só acontece com a chave certa.
 */
public class OneTimePadBreaker {

    public static void run(String path) throws IOException {
        // Lê todas as linhas do arquivo, ignorando linhas em branco.
        List<String> lines = Files.readAllLines(Path.of(path)).stream()
                .filter(line -> !line.isBlank())
                .toList();
        
        // Converte cada linha de hexadecimal para bytes.
        byte[][] ciphertexts = new byte[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            ciphertexts[i] = OneTimePadCipher.fromHex(lines.get(i).trim());
        }

        System.out.println("Mensagens cifradas com a mesma chave: " + ciphertexts.length);

        // Determina o tamanho máximo das mensagens para gerar uma chave suficientemente longa.
        int maxLength = 0;
        for (byte[] c : ciphertexts) {
            maxLength = Math.max(maxLength, c.length);
        }

        // Recupera cada byte da chave, um por vez, exigindo concordância unânime (ou quase) entre todas as mensagens disponíveis naquela posição.
        Byte[] key = new Byte[maxLength];
        for (int pos = 0; pos < maxLength; pos++) {
            key[pos] = recoverKeyByteAt(ciphertexts, pos);
        }

        // Decifra todas as mensagens com a chave recuperada (ou parcialmente recuperada).
        String[] plaintexts = decryptAll(ciphertexts, key);

        System.out.println();
        System.out.println("--- Textos decifrados (? = byte não recuperado nesta posição) ---");
        for (String p : plaintexts) {
            System.out.println(p);
        }

        checkAgainstReference(plaintexts);
    }

    // Recupera o byte da chave na posição pos, se houver concordância unânime (ou quase) entre todas as mensagens disponíveis naquela posição.
    private static Byte recoverKeyByteAt(byte[][] ciphertexts, int pos) {
        List<Integer> available = new ArrayList<>();
        for (int i = 0; i < ciphertexts.length; i++) {
            if (pos < ciphertexts[i].length) {
                available.add(i);
            }
        }
        if (available.size() < 3) {
            return null;
        }

        // Para cada valor possível de byte de chave (0-255), decifra o byte cifrado de cada mensagem disponível e conta quantos deles resultam em letras ou espaço. O byte de chave que obtiver concordância unânime (ou quase) é escolhido como o candidato.
        int bestKeyGuess = -1;
        int bestScore = -1;
        boolean tie = false;

        // Testa todos os 256 valores possíveis de byte de chave.
        for (int guess = 0; guess < 256; guess++) {
            int score = 0;
            for (int i : available) {
                char candidate = decodeChar(ciphertexts[i][pos], (byte) guess);
                if (isPlausiblePlainChar(candidate)) {
                    score++;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestKeyGuess = guess;
                tie = false;
            } else if (score == bestScore) {
                tie = true;
            }
        }

        // Exige concordância unânime (ou quase) e um vencedor sem empate.
        boolean confident = !tie && bestScore >= available.size() - 1;
        return confident ? (byte) bestKeyGuess : null;
    }

    // Une o byte cifrado com o byte de chave e trata o resultado como Latin-1 (0-255),
    // evitando que Java estenda o sinal de um byte negativo para um char de outro alfabeto.
    private static char decodeChar(byte cipherByte, byte keyByte) {
        return (char) ((cipherByte ^ keyByte) & 0xFF);
    }

    // Restrito a letras ASCII e espaço (o alfabeto real das mensagens). Usar Character.isLetter
    // seria amplo demais: aceitaria também letras acentuadas Latin-1 (À-ÿ), inflando muito as
    // coincidências e quebrando a concordância unânime entre mensagens.
    private static boolean isPlausiblePlainChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == ' ';
    }

    // --- Decifra todas as mensagens com a chave recuperada (ou parcialmente recuperada). ---
    private static String[] decryptAll(byte[][] ciphertexts, Byte[] key) {
        String[] plaintexts = new String[ciphertexts.length];
        for (int i = 0; i < ciphertexts.length; i++) {
            StringBuilder sb = new StringBuilder(ciphertexts[i].length);
            for (int pos = 0; pos < ciphertexts[i].length; pos++) {
                Byte k = key[pos];
                sb.append(k == null ? '?' : decodeChar(ciphertexts[i][pos], k));
            }
            plaintexts[i] = sb.toString();
        }
        return plaintexts;
    }

    // --- Conferência opcional contra o arquivo de texto original, só para validar o ataque. ---
    private static void checkAgainstReference(String[] plaintexts) {
        Path referencePath = Path.of("data/otp_plaintext.txt");
        if (!Files.exists(referencePath)) {
            return;
        }
        try {
            List<String> reference = Files.readAllLines(referencePath).stream()
                    .filter(line -> !line.isBlank())
                    .toList();

            int matchedChars = 0;
            int totalChars = 0;
            for (int i = 0; i < Math.min(reference.size(), plaintexts.length); i++) {
                String expected = reference.get(i);
                String got = plaintexts[i];
                for (int pos = 0; pos < expected.length(); pos++) {
                    totalChars++;
                    if (pos < got.length() && got.charAt(pos) == expected.charAt(pos)) {
                        matchedChars++;
                    }
                }
            }

            System.out.println();
            System.out.printf("Conferência com data/otp_plaintext.txt: %d/%d caracteres corretos (%.1f%%)%n",
                    matchedChars, totalChars, 100.0 * matchedChars / totalChars);
        } catch (IOException e) {
            // Arquivo de referência é só para conferência; sem ele o ataque continua válido.
        }
    }
}
