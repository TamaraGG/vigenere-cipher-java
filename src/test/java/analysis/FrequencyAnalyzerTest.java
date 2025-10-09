package analysis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FrequencyAnalyzerTest {

    @Test
    @DisplayName("Анализатор должен найти правильный ключ ('F') для текста, зашифрованного Цезарем")
    void findMostLikelyKeyChar_GivenCaesarCipherText_ShouldReturnCorrectKey() {
        FrequencyAnalyzer analyzer = new FrequencyAnalyzer();

        // Это осмысленный текст, зашифрованный сдвигом 'F' (сдвиг на 5)
        String caesarCipherText = "YMNXNXFNXNRUQJXYJSYJSFHQJSYJXYNSLTZWUTXJX";
        char expectedKey = 'F';

        char foundKey = analyzer.findMostLikelyKeyChar(caesarCipherText);
        assertEquals(expectedKey, foundKey);
    }
}