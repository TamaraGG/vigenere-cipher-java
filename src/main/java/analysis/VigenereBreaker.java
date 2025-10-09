package analysis;

import cipher.VigenereCipher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс, реализующий полный процесс взлома шифра Виженера. Он последовательно
 * определяет длину ключа и его состав, используя комбинацию статистических методов.
 * (Версия с улучшенным определением длины ключа)
 */
public class VigenereBreaker {

    // Зависимости от других классов: FrequencyAnalyzer для анализа колонок текста
    // и VigenereCipher для финальной расшифровки.
    private final FrequencyAnalyzer frequencyAnalyzer;
    private final VigenereCipher vigenereCipher;

    // Константы, определяющие параметры криптоанализа.
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    // Эталонное значение Индекса Совпадений для английского языка.
    private static final double ENGLISH_IC = 0.065;
    // Минимальная и максимальная длина ключа, которую будет проверять анализатор.
    private static final int MIN_KEY_LENGTH = 2;
    private static final int MAX_KEY_LENGTH = 20;

    // Конструктор класса, инициализирующий необходимые объекты.
    public VigenereBreaker() {
        this.frequencyAnalyzer = new FrequencyAnalyzer();
        this.vigenereCipher = new VigenereCipher();
    }

    /**
     * Главный публичный метод, который запускает и координирует весь процесс взлома.
     * Он последовательно выполняет три шага: подготовка текста, определение
     * длины ключа и определение самого ключа, после чего выполняет финальную расшифровку.
     * Промежуточные результаты выводятся в консоль для наглядности.
     */
    public String breakCipher(String cipherText) {
        String preparedText = prepareText(cipherText);

        System.out.println("Analyzing key length...");
        int keyLength = findKeyLength(preparedText);
        System.out.println("Most likely key length found: " + keyLength);

        System.out.println("Finding the key...");
        String key = findKey(preparedText, keyLength);
        System.out.println("Key found: " + key);

        System.out.println("Final decryption...");
        return vigenereCipher.decrypt(preparedText, key);
    }

    /**
     * Определяет наиболее вероятную длину ключа. Работает в несколько этапов:
     * 1. Для каждой возможной длины ключа вычисляется средний Индекс Совпадений (IC).
     * 2. Находится длина, для которой средний IC наиболее близок к эталонному значению.
     * 3. (Улучшение) Проверяются делители найденной длины-кандидата. Истинная длина ключа
     *    часто является наименьшим делителем, который также показывает высокий IC.
     *    Это позволяет избежать выбора длин, кратных истинной (например, 15 вместо 5).
     */
    private int findKeyLength(String text) {
        Map<Integer, Double> allScores = new HashMap<>();

        // Шаг 1: Рассчитываем средний IC для каждой возможной длины ключа.
        for (int keyLength = MIN_KEY_LENGTH; keyLength <= MAX_KEY_LENGTH; keyLength++) {
            StringBuilder[] streams = new StringBuilder[keyLength];
            for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
            for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

            double totalIc = 0.0;
            for (int i = 0; i < keyLength; i++) {
                totalIc += calculateIndexOfCoincidence(streams[i].toString());
            }
            allScores.put(keyLength, totalIc / keyLength);
        }

        // Шаг 2: Находим длину с лучшим (наиболее близким к английскому) IC.
        int bestScoringLength = 0;
        double bestScore = 0.0;
        for (Map.Entry<Integer, Double> entry : allScores.entrySet()) {
            if (Math.abs(entry.getValue() - ENGLISH_IC) < Math.abs(bestScore - ENGLISH_IC)) {
                bestScore = entry.getValue();
                bestScoringLength = entry.getKey();
            }
        }

        // Шаг 3: Проверяем делители лучшего кандидата.
        List<Integer> factors = getFactors(bestScoringLength);
        for (int factor : factors) {
            // Проверяем, является ли IC для этого делителя также "хорошим".
            if (allScores.getOrDefault(factor, 0.0) > (ENGLISH_IC - 0.01)) {
                // Возвращаем первый же "хороший" делитель, он будет самым маленьким.
                return factor;
            }
        }

        // Если ни один из делителей не подходит, возвращаем лучший изначальный вариант.
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
     * Метод для определения состава ключа, когда его длина уже известна.
     * Он разделяет шифротекст на потоки (колонки) в соответствии с длиной ключа.
     * Затем для каждой колонки с помощью FrequencyAnalyzer находится наиболее
     * вероятный символ ключа. Найденные символы объединяются в итоговый ключ.
     */
    private String findKey(String text, int keyLength) {
        StringBuilder[] streams = new StringBuilder[keyLength];
        for (int i = 0; i < keyLength; i++) streams[i] = new StringBuilder();
        for (int i = 0; i < text.length(); i++) streams[i % keyLength].append(text.charAt(i));

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