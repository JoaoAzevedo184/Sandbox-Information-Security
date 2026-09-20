import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.List;

/**
 * Gera o desafio do exercício 3: 7 mensagens distintas cifradas com a MESMA chave
 * (reuso de chave em One-Time Pad, o erro clássico que permite o ataque de "many-time pad").
 * Executar uma única vez; os arquivos gerados ficam versionados em data/.
 */
public class OneTimePadChallengeGenerator {

    private static final List<String> MESSAGES = List.of(
            "A confidencialidade so e garantida se a chave nunca for reutilizada.",
            "One time pad e perfeito na teoria mas fragil na pratica.",
            "Reusar a chave permite recuperar as mensagens originais sem forca bruta.",
            "Criptografia de fluxo tambem sofre com reuso de chave, assim como o OTP.",
            "A equipe de seguranca revisou o protocolo apos o incidente.",
            "Nunca envie duas mensagens distintas cifradas com a mesma chave.",
            "Este e o ultimo texto do conjunto usado no ataque ao esquema."
    );

    public static void main(String[] args) throws IOException {
        // Gera uma chave aleatória de tamanho suficiente para todas as mensagens, e depois cifra cada mensagem com a mesma chave.
        SecureRandom random = new SecureRandom();

        // Determina o tamanho máximo das mensagens para gerar uma chave suficientemente longa.
        int maxLength = MESSAGES.stream().mapToInt(String::length).max().orElseThrow();
        byte[] key = new byte[maxLength];
        random.nextBytes(key);

        // Gera os arquivos de saída: data/otp_plaintext.txt (mensagens originais) e data/otp_texto.txt (mensagens cifradas em hexadecimal).
        StringBuilder plaintextOut = new StringBuilder();
        StringBuilder ciphertextOut = new StringBuilder();

        // Cifra cada mensagem com a mesma chave (reuso de chave).
        for (String message : MESSAGES) {
            byte[] plainBytes = message.getBytes(StandardCharsets.US_ASCII);
            byte[] keySlice = new byte[plainBytes.length];
            System.arraycopy(key, 0, keySlice, 0, plainBytes.length);

            byte[] cipherBytes = OneTimePadCipher.xor(plainBytes, keySlice);

            plaintextOut.append(message).append(System.lineSeparator());
            ciphertextOut.append(OneTimePadCipher.toHex(cipherBytes)).append(System.lineSeparator());
        }

        // Salva os arquivos de saída.
        Files.writeString(Path.of("data/otp_plaintext.txt"), plaintextOut.toString());
        Files.writeString(Path.of("data/otp_texto.txt"), ciphertextOut.toString());

        System.out.println("Desafio gerado: data/otp_texto.txt (" + MESSAGES.size() + " mensagens, mesma chave).");
    }
}
