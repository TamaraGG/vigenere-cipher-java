package cipher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VigenereCipherTest {
    private VigenereCipher vigenereCipher;

    @BeforeEach
    void setUp() {
        // Создаем новый экземпляр перед каждым тестом
        vigenereCipher = new VigenereCipher();
    }

    @Test
    @DisplayName("Простое шифрование текста и ключа")
    void encrypt_SimpleTextAndKey_ShouldReturnCorrectCipherText() {
        String plainText = "HELLOWORLD";
        String key = "KEY";
        String expected = "RIJVSUYVJN";
        assertEquals(expected, vigenereCipher.encrypt(plainText, key));
    }

    @Test
    @DisplayName("Простая расшифровка текста и ключа")
    void decrypt_SimpleCipherTextAndKey_ShouldReturnCorrectPlainText() {
        String cipherText = "RIJVSUYVJN";
        String key = "KEY";
        String expected = "HELLOWORLD";
        assertEquals(expected, vigenereCipher.decrypt(cipherText, key));
    }

    @Test
    @DisplayName("Шифрование текста с пробелами, цифрами и знаками препинания")
    void encrypt_TextWithMixedChars_ShouldIgnoreNonAlphabetic() {
        String plainText = "Hello, World! 123";
        String key = "KEY";
        String expected = "RIJVSUYVJN"; // Должен быть такой же, как и для "HELLOWORLD"
        assertEquals(expected, vigenereCipher.encrypt(plainText, key));
    }

    @Test
    @DisplayName("Ключ с пробелами и в разном регистре должен быть нормализован")
    void encrypt_KeyWithMixedChars_ShouldBeNormalized() {
        String plainText = "HELLOWORLD";
        String key = "k E y";
        String expected = "RIJVSUYVJN";
        assertEquals(expected, vigenereCipher.encrypt(plainText, key));
    }

    @Test
    @DisplayName("Пустой ключ должен вызывать исключение")
    void encrypt_EmptyKey_ShouldThrowException() {
        String plainText = "SOME TEXT";
        String key = "123 !"; // После нормализации ключ станет пустым
        assertThrows(IllegalArgumentException.class, () -> {
            vigenereCipher.encrypt(plainText, key);
        });
    }
}