package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Вспомогательный (сервисный) класс, который инкапсулирует всю логику
 * для работы с файловой системой. Его основная задача — предоставить
 * простые статические методы для чтения и записи файлов, обрабатывая
 * возможные исключения внутри.
 */
public class FileService {

    /**
     * Метод для чтения всего содержимого файла в одну строку.
     * Использует современный API `java.nio.file` для эффективности.
     * В случае ошибки (например, файл не найден) выводит сообщение
     * в поток ошибок и возвращает `null`, что позволяет основной
     * программе корректно обработать сбой.
     */
    public static String readFile(String filePath) {
        try {
            // Преобразует строковый путь в объект Path и читает файл.
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            // Обработка исключений ввода-вывода.
            System.err.println("Error reading file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Метод для записи строки в файл. Если файл не существует, он будет
     * создан. Если файл уже существует, его содержимое будет полностью
     * перезаписано. Возвращает `true` в случае успеха и `false`
     * в случае ошибки, выводя ее детали в поток ошибок.
     */
    public static boolean writeFile(String filePath, String content) {
        try {
            // Записывает строку в файл, используя опции для создания
            // или перезаписи существующего файла.
            Files.writeString(Paths.get(filePath), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            // Обработка исключений ввода-вывода.
            System.err.println("Error writing to file: " + filePath);
            System.err.println("Details: " + e.getMessage());
            return false;
        }
    }
}