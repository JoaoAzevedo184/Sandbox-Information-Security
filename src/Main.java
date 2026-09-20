import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println();
        System.out.println("=== Ataque ao esquema One-Time Pad (chave reutilizada) ===");
        String otpPath = args.length > 1 ? args[1] : "data/otp_texto.txt";
        OneTimePadBreaker.run(otpPath);
    }

    /**
     * Executa o ataque à cifra de Vigenère, descobrindo a chave e decifrando o texto.
     */
    public static void runVigenereAtack(String[] args) throws IOException {
        System.out.println();
        System.out.println("=== Ataque à cifra de Vigenère ===");
        String vigenerePath = args.length > 0 ? args[0] : "data/text.txt";
        VigenereBreaker.run(vigenerePath);
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
