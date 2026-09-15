public class VigenereCipher {

    public static String encrypt(String text, String key) {
        return process(text, key, true);
    }

    public static String decrypt(String text, String key) {
        return process(text, key, false);
    }

    private static String process(String text, String key, boolean encrypting) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("A chave não pode estar vazia.");
        }

        StringBuilder result = new StringBuilder(text.length());
        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (Character.isLetter(c)) {
                char keyChar = Character.toUpperCase(key.charAt(keyIndex % key.length()));
                int shift = keyChar - 'A';
                if (!encrypting) {
                    shift = -shift;
                }

                boolean upperCase = Character.isUpperCase(c);
                char base = upperCase ? 'A' : 'a';
                char shifted = (char) (((c - base + shift + 26) % 26) + base);

                result.append(shifted);
                keyIndex++;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
