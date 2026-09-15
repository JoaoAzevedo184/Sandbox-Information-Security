import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println();
        System.out.println("=== Ataque à cifra de Vigenère ===");
        String path = args.length > 0 ? args[0] : "data/text.txt";
        VigenereBreaker.run(path);
    }

    /**
     * Executa uma demonstração da cifra de Vigenère, cifrando e decifrando um texto com uma chave conhecida.
     */
    private static void runVigenereDemo() {
        String key = "chave";
        String texto = "Quero comer muito cuscuz e feijoada, mas tenho que me controlar porque estou de dieta.";

        String cipherText = VigenereCipher.encrypt(texto, key);
        String decryptedText = VigenereCipher.decrypt(cipherText, key);

        System.out.println("Texto original: " + texto);
        System.out.println("Chave: " + key);
        System.out.println("Texto cifrado: " + cipherText);
        System.out.println("Texto decifrado: " + decryptedText);
    }
}
