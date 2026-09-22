import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Conta colisões parciais em funções hash (paradoxo do aniversário).
 * Gera entradas aleatórias, calcula SHA-256 e trunca o digest para os
 * primeiros N bits, contando quantas vezes esse valor truncado se repete.
 */
public class HashCollisionCounter {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        selfTest();

        int bits = args.length > 0 ? Integer.parseInt(args[0]) : 16;
        int trials = args.length > 1 ? Integer.parseInt(args[1]) : 100_000;

        run(bits, trials);
    }

    static void run(int bits, int trials) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        SecureRandom random = new SecureRandom();
        Set<Long> seen = new HashSet<>();

        int collisions = 0;
        int firstCollisionAt = -1;

        for (int i = 1; i <= trials; i++) {
            byte[] input = new byte[16];
            random.nextBytes(input);
            byte[] digest = sha256.digest(input);
            long truncated = truncateBits(digest, bits);

            if (!seen.add(truncated)) {
                collisions++;
                if (firstCollisionAt == -1) {
                    firstCollisionAt = i;
                }
            }
        }

        double expectedFirstCollision = Math.sqrt(Math.PI / 2 * Math.pow(2, bits));

        System.out.println();
        System.out.println("=== Colisões parciais em funções hash (SHA-256, " + bits + " bits) ===");
        System.out.println("Tentativas: " + trials);
        System.out.println("Colisões observadas: " + collisions);
        System.out.println("Primeira colisão na tentativa: " +
                (firstCollisionAt == -1 ? "nenhuma" : firstCollisionAt));
        System.out.printf("Estimativa teórica (aniversário) para 1ª colisão: ~%.0f tentativas%n",
                expectedFirstCollision);
    }

    /** Extrai os `bits` bits mais significativos do digest como long (bits <= 63). */
    static long truncateBits(byte[] digest, int bits) {
        int bytesNeeded = (bits + 7) / 8;
        long value = 0;
        for (int i = 0; i < bytesNeeded; i++) {
            value = (value << 8) | (digest[i] & 0xFF);
        }
        int extraBits = bytesNeeded * 8 - bits;
        return value >>> extraBits;
    }

    private static void selfTest() {
        byte[] digest = {(byte) 0xAB, (byte) 0xCD, (byte) 0x12};
        assert truncateBits(digest, 8) == 0xAB : "8 bits deve ser o primeiro byte";
        assert truncateBits(digest, 4) == 0xA : "4 bits deve ser o nibble alto do primeiro byte";
        assert truncateBits(digest, 16) == 0xABCD : "16 bits deve ser os dois primeiros bytes";
    }
}
