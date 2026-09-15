public class Main {
    public static void main(String[] args) {
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
