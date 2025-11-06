package analysis;

import java.util.Map;

/**
 * класс-контейнер для хранения результатов частотного анализа.
 * Содержит найденный символ ключа и карту с оценками для всех возможных символов.
 */
public record AnalysisResult(char bestKeyChar, Map<Character, Double> chiSquaredScores) {
}