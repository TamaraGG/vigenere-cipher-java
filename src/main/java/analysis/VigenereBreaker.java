package analysis;

import analysis.FrequencyAnalyzer;
import cipher.VigenereCipher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс, реализующий полный процесс взлома шифра Виженера. Он последовательно
 * определяет длину ключа и его состав, используя комбинацию статистических методов.
 */
public class VigenereBreaker {

    // Объекты-помощники для выполнения анализа и расшифровки.
    private final FrequencyAnalyzer frequencyAnalyzer;
    private final VigenereCipher vigenereCipher;

    // Минимальная длина ключа для проверки. Установлена в 1 для поддержки шифра Цезаря.
    public static final int MIN_KEY_LENGTH = 1;

    // Минимальное количество символов в "колонке" для надежной статистики.
    private static final int MIN_STREAM_LENGTH_FOR_STATS = 50;
    // Абсолютный потолок, чтобы избежать слишком долгих вычислений на огромных файлах.
    private static final int ABSOLUTE_MAX_KEY_LENGTH = 50;

    // Константы для анализа: алфавит и эталонный Индекс Совпадений для английского языка.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final double ENGLISH_IC = 0.065;

    /**
     * Конструктор класса, инициализирующий необходимые объекты.
     */
    public VigenereBreaker() {
        this.frequencyAnalyzer = new FrequencyAnalyzer();
        this.vigenereCipher = new VigenereCipher();
    }

    /**
     * Рассчитывает максимальную длину ключа для проверки, исходя из длины текста.
     * Это делается, чтобы обеспечить достаточно данных для надежного анализа.
     * @param textLength Длина зашифрованного текста.
     * @return Рассчитанная максимальная длина ключа.
     */
    public static int calculateMaxKeyLength(int textLength) {
        // Рассчитываем лимит, деля общую длину на минимально необходимую для одной "колонки".
        int dynamicLimit = textLength / MIN_STREAM_LENGTH_FOR_STATS;
        // Возвращаем меньшее из двух: рассчитанный лимит или абсолютный потолок.
        return Math.min(dynamicLimit, ABSOLUTE_MAX_KEY_LENGTH);
    }

    /**
     * Главный метод, запускающий и координирующий весь процесс взлома.
     * Последовательно выполняет все шаги и выводит промежуточные результаты.
     */
    public String breakCipher(String cipherText) {
        // Шаг 1: Очистка текста от лишних символов.
        String preparedText = prepareText(cipherText);

        // Шаг 2: Определение наиболее вероятной длины ключа.
        System.out.println("Analyzing key length...");
        int keyLength = findKeyLength(preparedText);
        System.out.println("Most likely key length found: " + keyLength);

        // Шаг 3: Определение состава ключа (самих букв).
        System.out.println("Finding the key...");
        String key = findKey(preparedText, keyLength);
        System.out.println("Key found: " + key);

        // Шаг 4: Финальная расшифровка с найденным ключом.
        System.out.println("Final decryption...");
        return vigenereCipher.decrypt(preparedText, key);
    }

    /**
     * Определяет наиболее вероятную длину ключа методом Индекса Совпадений (IC).
     * 1. Для каждой возможной длины ключа вычисляется средний IC.
     * 2. Находится длина, для которой средний IC наиболее близок к эталонному (0.065).
     * 3. Проверяются делители найденной длины, чтобы избежать выбора длин, кратных
     *    истинной (например, 10 вместо 5).
     */
    private int findKeyLength(String text) {
        // Динамически определяем максимальную длину ключа для поиска.
        int maxKeyToTest = calculateMaxKeyLength(text.length());

        // Обработка очень коротких текстов: пытаемся взломать как шифр Цезаря.
        if (maxKeyToTest < MIN_KEY_LENGTH) {
            System.out.println("Warning: Ciphertext is too short for a reliable analysis. Defaulting to Caesar cipher check (key length 1).");
            maxKeyToTest = 1;
        }

        Map<Integer, Double> allScores = new HashMap<>();

        // Перебираем все возможные длины ключа от 1 до рассчитанного максимума.
        for (int keyLength = MIN_KEY_LENGTH; keyLength <= maxKeyToTest; keyLength++) {
            // Разделяем текст на "колонки" (потоки).
            StringBuilder[] streams = new StringBuilder[keyLength];
            for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
            for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

            // Считаем средний IC для данной длины ключа.
            double totalIc = 0.0;
            for (int i = 0; i < keyLength; i++) {
                totalIc += calculateIndexOfCoincidence(streams[i].toString());
            }
            allScores.put(keyLength, totalIc / keyLength);
        }

        // Находим длину с самым "лучшим" показателем IC.
        int bestScoringLength = 0;
        double bestScore = -1.0;
        for (Map.Entry<Integer, Double> entry : allScores.entrySet()) {
            if (bestScore < 0 || Math.abs(entry.getValue() - ENGLISH_IC) < Math.abs(bestScore - ENGLISH_IC)) {
                bestScore = entry.getValue();
                bestScoringLength = entry.getKey();
            }
        }

        // Улучшение: проверяем делители лучшего кандидата.
        if (bestScoringLength > 1) {
            List<Integer> factors = getFactors(bestScoringLength);
            for (int factor : factors) {
                // Если у делителя тоже высокий IC, скорее всего, он и есть истинная длина.
                if (allScores.getOrDefault(factor, 0.0) > (ENGLISH_IC - 0.01)) {
                    return factor;
                }
            }
        }

        return bestScoringLength;
    }

    /**
     * Вспомогательный метод для нахождения всех делителей числа (кроме 1).
     */
    private List<Integer> getFactors(int number) {
        List<Integer> factors = new ArrayList<>();
        for (int i = 2; i * i <= number; i++) {
            if (number % i == 0) {
                factors.add(i);
                if (i * i != number) {
                    factors.add(number / i);
                }
            }
        }
        factors.sort(Integer::compareTo);
        return factors;
    }

    /**
     * Реализует математический расчет Индекса Совпадений для фрагмента текста.
     */
    private double calculateIndexOfCoincidence(String text) {
        if (text.length() < 2) return 0.0;
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : text.toCharArray()) counts.put(c, counts.getOrDefault(c, 0) + 1);
        double numerator = 0.0;
        for (int count : counts.values()) numerator += count * (count - 1.0);
        double denominator = text.length() * (text.length() - 1.0);
        return numerator / denominator;
    }

    /**
     * Определяет состав ключа, когда его длина уже известна.
     * Разделяет шифротекст на "колонки" и для каждой из них находит
     * наиболее вероятный символ ключа с помощью частотного анализа.
     */
    private String findKey(String text, int keyLength) {
        // Разделяем текст на "колонки" по числу букв в ключе.
        StringBuilder[] streams = new StringBuilder[keyLength];
        for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
        for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

        // Для каждой "колонки" находим свою букву ключа.
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            key.append(frequencyAnalyzer.findMostLikelyKeyChar(streams[i].toString()));
        }
        return key.toString();
    }

    /**
     * Вспомогательный метод для очистки текста: удаляет все символы, кроме букв,
     * и приводит текст к верхнему регистру.
     */
    private String prepareText(String text) {
        StringBuilder sb = new StringBuilder();
        for (char symbol : text.toUpperCase().toCharArray()) {
            if (ALPHABET.indexOf(symbol) != -1) sb.append(symbol);
        }
        return sb.toString();
    }
}