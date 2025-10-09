package analysis;

import cipher.VigenereCipher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VigenereBreakerTest {
    private VigenereBreaker vigenereBreaker;
    private VigenereCipher vigenereCipher;

    @BeforeEach
    void setUp() {
        vigenereBreaker = new VigenereBreaker();
        vigenereCipher = new VigenereCipher();
    }

    @Test
    void testFullBreakCipherSuccess() {
        // Используем достаточно длинный английский текст для надежности статистики
        String originalText = "Cryptography, or cryptology, is the practice and study of techniques for " +
                "secure communication in the presence of third parties called adversaries. " +
                "More generally, cryptography is about constructing and analyzing protocols " +
                "that prevent third parties or the public from reading private messages.";

        String key = "CRYPTO";

        // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
        // 1. Готовим текст для сравнения, используя публичные методы.
        // Шифруем и сразу дешифруем с простым ключом "A".
        // Результатом будет текст, прошедший через внутренний метод prepareText.
        String expectedPlainText = vigenereCipher.decrypt(vigenereCipher.encrypt(originalText, "A"), "A");

        // 2. Шифруем текст, чтобы получить "жертву" для взлома
        String cipherText = vigenereCipher.encrypt(originalText, key);

        System.out.println("--- Starting Vigenere Breaker Test ---");
        System.out.println("Original Key: " + key);
        System.out.println("Ciphertext length: " + cipherText.length());

        // 3. Взламываем шифр
        // Мы передаем в метод "грязный" шифротекст, так как сам метод breakCipher
        // внутри себя вызывает свой собственный prepareText.
        String brokenText = vigenereBreaker.breakCipher(cipherText);

        System.out.println("--- Test Finished ---");

        // 4. Проверяем, что взломанный текст совпадает с ожидаемым подготовленным текстом
        assertEquals(expectedPlainText, brokenText);
    }
}