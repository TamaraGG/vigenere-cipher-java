package cipher;

/**
 * Класс, реализующий основную логику шифра Виженера. Включает в себя методы
 * для шифрования и дешифрования текста, работая исключительно с английским алфавитом.
 */
public class VigenereCipher {

    // Константа, определяющая алфавит, на котором производятся все криптографические операции.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Вспомогательный метод, выполняющий предварительную обработку текста.
     * Он удаляет все символы, не являющиеся буквами английского алфавита,
     * и приводит оставшиеся символы к верхнему регистру. Это необходимо
     * для корректной работы математической модели шифра.
     */
    private String prepareText(String text) {
        StringBuilder sb = new StringBuilder();
        for (char symbol : text.toUpperCase().toCharArray()) {
            if (ALPHABET.indexOf(symbol) != -1) {
                sb.append(symbol);
            }
        }
        return sb.toString();
    }

    /**
     * Метод, выполняющий шифрование открытого текста с использованием заданного ключа.
     * Процесс включает подготовку текста и ключа, а затем посимвольное применение
     * математической формулы шифра Виженера.
     */
    public String encrypt(String plainText, String key) {
        // Подготовка текста и ключа к стандартному виду.
        String preparedText = prepareText(plainText);
        String preparedKey = prepareText(key);

        if (preparedKey.isEmpty()) {
            // Проверка на случай, если ключ не содержит букв.
            throw new IllegalArgumentException("Key must contain at least one letter.");
        }

        StringBuilder cipherText = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < preparedText.length(); i++) {
            // Получаем числовые эквиваленты символов (A=0, B=1, ...).
            int plainTextCharIndex = ALPHABET.indexOf(preparedText.charAt(i));
            int keyCharIndex = ALPHABET.indexOf(preparedKey.charAt(keyIndex));

            // Реализация основной формулы шифрования: C = (P + K) mod 26.
            int cipherCharIndex = (plainTextCharIndex + keyCharIndex) % ALPHABET.length();

            cipherText.append(ALPHABET.charAt(cipherCharIndex));

            // Перемещаем индекс ключа для циклического повторения.
            keyIndex = (keyIndex + 1) % preparedKey.length();
        }

        return cipherText.toString();
    }

    /**
     * Метод, выполняющий расшифровку шифротекста. Является обратной операцией
     * к шифрованию и использует ту же логику циклического применения ключа,
     * но с обратной математической формулой.
     */
    public String decrypt(String cipherText, String key) {
        String preparedText = prepareText(cipherText);
        String preparedKey = prepareText(key);

        if (preparedKey.isEmpty()) {
            throw new IllegalArgumentException("Key must contain at least one letter.");
        }

        StringBuilder plainText = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < preparedText.length(); i++) {
            // Получаем числовые эквиваленты символов.
            int cipherTextCharIndex = ALPHABET.indexOf(preparedText.charAt(i));
            int keyCharIndex = ALPHABET.indexOf(preparedKey.charAt(keyIndex));

            // Реализация формулы расшифровки: P = (C - K + 26) mod 26.
            // Добавление длины алфавита необходимо для корректной обработки
            // отрицательных результатов при вычитании в Java.
            int plainTextCharIndex = (cipherTextCharIndex - keyCharIndex + ALPHABET.length()) % ALPHABET.length();

            plainText.append(ALPHABET.charAt(plainTextCharIndex));

            // Перемещаем индекс ключа.
            keyIndex = (keyIndex + 1) % preparedKey.length();
        }

        return plainText.toString();
    }
}