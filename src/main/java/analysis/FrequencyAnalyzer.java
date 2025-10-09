package analysis;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс, реализующий логику частотного анализа. Основная задача — определить
 * наиболее вероятный ключ (состоящий из одного символа) для текста,
 * зашифрованного простым сдвигом (шифром Цезаря).
 */
public class FrequencyAnalyzer {

    // Статическая карта, хранящая эталонные частоты появления букв в английском языке.
    // Эти данные используются в качестве основы для сравнения при криптоанализе.
    private static final Map<Character, Double> ENGLISH_FREQUENCIES = new HashMap<>();
    static {
        ENGLISH_FREQUENCIES.put('A', 0.08167); ENGLISH_FREQUENCIES.put('B', 0.01492);
        ENGLISH_FREQUENCIES.put('C', 0.02782); ENGLISH_FREQUENCIES.put('D', 0.04253);
        ENGLISH_FREQUENCIES.put('E', 0.12702); ENGLISH_FREQUENCIES.put('F', 0.02228);
        ENGLISH_FREQUENCIES.put('G', 0.02015); ENGLISH_FREQUENCIES.put('H', 0.06094);
        ENGLISH_FREQUENCIES.put('I', 0.06966); ENGLISH_FREQUENCIES.put('J', 0.00153);
        ENGLISH_FREQUENCIES.put('K', 0.00772); ENGLISH_FREQUENCIES.put('L', 0.04025);
        ENGLISH_FREQUENCIES.put('M', 0.02406); ENGLISH_FREQUENCIES.put('N', 0.06749);
        ENGLISH_FREQUENCIES.put('O', 0.07507); ENGLISH_FREQUENCIES.put('P', 0.01929);
        ENGLISH_FREQUENCIES.put('Q', 0.00095); ENGLISH_FREQUENCIES.put('R', 0.05987);
        ENGLISH_FREQUENCIES.put('S', 0.06327); ENGLISH_FREQUENCIES.put('T', 0.09056);
        ENGLISH_FREQUENCIES.put('U', 0.02758); ENGLISH_FREQUENCIES.put('V', 0.00978);
        ENGLISH_FREQUENCIES.put('W', 0.02360); ENGLISH_FREQUENCIES.put('X', 0.00150);
        ENGLISH_FREQUENCIES.put('Y', 0.01974); ENGLISH_FREQUENCIES.put('Z', 0.00074);
    }

    // Константа, определяющая используемый алфавит.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Основной метод анализатора. Он последовательно перебирает все 26 букв алфавита
     * в качестве возможного ключа. Для каждого ключа он расшифровывает текст и вычисляет,
     * насколько результат похож на осмысленный английский текст с помощью теста "Хи-квадрат".
     * Ключ, давший наилучший результат (наименьшее значение "Хи-квадрат"),
     * возвращается как наиболее вероятный.
     */
    public char findMostLikelyKeyChar(String subtext) {
        double minChiSquared = Double.POSITIVE_INFINITY;
        char bestKeyChar = 'A';

        // Перебираем все возможные сдвиги (все 26 букв алфавита как ключ).
        for (int i = 0; i < ALPHABET.length(); i++) {
            char potentialKeyChar = ALPHABET.charAt(i);
            String decryptedText = decryptCaesar(subtext, potentialKeyChar);

            // Считаем статистику для расшифрованного текста.
            double chiSquared = calculateChiSquared(decryptedText);

            // Если текущая статистика лучше (меньше), чем лучшая из найденных,
            // обновляем лучшую статистику и лучший ключ.
            if (chiSquared < minChiSquared) {
                minChiSquared = chiSquared;
                bestKeyChar = potentialKeyChar;
            }
        }
        return bestKeyChar;
    }

    /**
     * Реализует статистический тест "Хи-квадрат". Метод сравнивает частотное
     * распределение букв в анализируемом тексте с эталонным распределением
     * для английского языка. Чем меньше итоговое значение, тем больше текст
     * похож на осмысленный.
     */
    private double calculateChiSquared(String text) {
        if (text.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }

        // Подсчитываем количество вхождений каждой буквы в тексте.
        Map<Character, Integer> letterCounts = new HashMap<>();
        for (char c : ALPHABET.toCharArray()) {
            letterCounts.put(c, 0);
        }
        for (char c : text.toCharArray()) {
            letterCounts.put(c, letterCounts.get(c) + 1);
        }

        double chiSquared = 0.0;
        int textLength = text.length();

        for (char c : ALPHABET.toCharArray()) {
            // Ожидаемое количество вхождений буквы в тексте данной длины.
            double expectedCount = textLength * ENGLISH_FREQUENCIES.get(c);
            // Наблюдаемое (фактическое) количество.
            double observedCount = letterCounts.get(c);

            // Вычисляем значение по формуле Хи-квадрат и суммируем.
            chiSquared += Math.pow(observedCount - expectedCount, 2) / expectedCount;
        }

        return chiSquared;
    }

    /**
     * Вспомогательный метод, выполняющий расшифровку шифра Цезаря. Необходим
     * для получения пробных вариантов открытого текста при переборе ключей.
     */
    private String decryptCaesar(String cipherText, char keyChar) {
        StringBuilder plainText = new StringBuilder();
        int keyIndex = ALPHABET.indexOf(keyChar);

        for (char symbol : cipherText.toCharArray()) {
            int symbolIndex = ALPHABET.indexOf(symbol);
            // Формула расшифровки для шифра Цезаря.
            int plainIndex = (symbolIndex - keyIndex + ALPHABET.length()) % ALPHABET.length();
            plainText.append(ALPHABET.charAt(plainIndex));
        }
        return plainText.toString();
    }
}