package analysis;

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
        String preparedText = prepareText(cipherText);
        if (preparedText.isEmpty()) {
            System.out.println("Cannot analyze an empty or non-alphabetic text.");
            return "";
        }

        System.out.println("\n--- Step 1: Analyzing Key Length ---");
        int keyLength = findKeyLength(preparedText);
        System.out.println("--- RESULT: Most likely key length is " + keyLength + " ---\n");

        System.out.println("--- Step 2: Finding Key Characters ---");
        String key = findKey(preparedText, keyLength);
        System.out.println("--- RESULT: Key found: \"" + key + "\" ---\n");

        System.out.println("--- Step 3: Final Decryption ---");
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
        int maxKeyToTest = calculateMaxKeyLength(text.length());

        if (maxKeyToTest < MIN_KEY_LENGTH) {
            System.out.println("Warning: Ciphertext is too short. Defaulting to Caesar cipher check (key length 1).");
            maxKeyToTest = 1;
        }

        Map<Integer, Double> allScores = new HashMap<>();
        System.out.println("Calculating average Index of Coincidence (IC) for each possible key length...");
        System.out.println("(Target IC for English text is approx. " + ENGLISH_IC + ")");

        for (int keyLength = MIN_KEY_LENGTH; keyLength <= maxKeyToTest; keyLength++) {
            StringBuilder[] streams = new StringBuilder[keyLength];
            for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
            for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

            double totalIc = 0.0;
            for (int i = 0; i < keyLength; i++) {
                totalIc += calculateIndexOfCoincidence(streams[i].toString());
            }
            double averageIc = totalIc / keyLength;
            allScores.put(keyLength, averageIc);
            System.out.println(String.format("  - Key Length %2d: Average IC = %.5f", keyLength, averageIc));
        }

        int bestScoringLength = 0;
        double bestScore = -1.0;
        for (Map.Entry<Integer, Double> entry : allScores.entrySet()) {
            if (bestScore < 0 || Math.abs(entry.getValue() - ENGLISH_IC) < Math.abs(bestScore - ENGLISH_IC)) {
                bestScore = entry.getValue();
                bestScoringLength = entry.getKey();
            }
        }
        System.out.println("\nInitial analysis suggests the best key length is " + bestScoringLength + " (IC score: " + String.format("%.5f", bestScore) + ")");

        if (bestScoringLength > 1) {
            List<Integer> factors = getFactors(bestScoringLength);
            if (!factors.isEmpty()) {
                System.out.println("Checking factors of " + bestScoringLength + " for harmonic errors: " + factors);
                for (int factor : factors) {
                    if (allScores.getOrDefault(factor, 0.0) > (ENGLISH_IC - 0.01)) { // Небольшой допуск
                        System.out.println("Factor " + factor + " also shows a high IC. It is a more likely candidate.");
                        return factor;
                    }
                }
                System.out.println("No smaller factor found with a high IC. Sticking with " + bestScoringLength + ".");
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
        StringBuilder[] streams = new StringBuilder[keyLength];
        for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
        for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

        StringBuilder key = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            System.out.println(String.format("  - Analyzing stream %d of %d...", i + 1, keyLength));

            AnalysisResult result = frequencyAnalyzer.findMostLikelyKeyChar(streams[i].toString());
            char foundChar = result.bestKeyChar();

            // Выводим ТОП-3 лучших кандидатов
            System.out.println("    Top 3 candidates (lower Chi-squared is better):");
            int count = 0;
            for (Map.Entry<Character, Double> entry : result.chiSquaredScores().entrySet()) {
                if (count < 3) {
                    System.out.println(String.format("      - Key '%c': score = %.2f", entry.getKey(), entry.getValue()));
                    count++;
                } else {
                    break;
                }
            }

            System.out.println(String.format("    >> Best match found: '%c'", foundChar));
            key.append(foundChar);
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