package ui;

import analysis.VigenereBreaker;
import cipher.VigenereCipher;
import services.FileService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Класс, отвечающий за взаимодействие с пользователем через консоль.
 * Управляет основным циклом программы, отображает меню, обрабатывает
 * ввод и координирует вызовы других компонентов.
 */
public class ConsoleUI {

    // Поля класса для хранения ключевых компонентов:
    // Scanner для чтения ввода, VigenereCipher для шифрования,
    // и VigenereBreaker для выполнения криптоанализа.
    private final Scanner scanner;
    private final VigenereCipher vigenereCipher;
    private final VigenereBreaker vigenereBreaker;

    /**
     * Конструктор класса. Инициализирует все необходимые объекты.
     */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.vigenereCipher = new VigenereCipher();
        this.vigenereBreaker = new VigenereBreaker();
    }

    /**
     * Основной метод, запускающий главный цикл программы. Отображает
     * меню, считывает выбор пользователя и вызывает соответствующий
     * метод-обработчик.
     */
    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    handleEncryption();
                    break;
                case "2":
                    handleDecryption();
                    break;
                case "3":
                    handleBreakCipher();
                    break;
                case "4":
                    System.out.println("Exiting the program.");
                    return; // Выход из метода и завершение программы.
                default:
                    System.out.println("Invalid input. Please choose an option from 1 to 4.");
                    break;
            }
            // Пауза, чтобы пользователь успел прочитать результат.
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    /**
     * Вспомогательный метод, который выводит в консоль главное меню.
     */
    private void printMenu() {
        System.out.println("\n--- Vigenere Cipher Tool ---");
        System.out.println("1. Encrypt a file");
        System.out.println("2. Decrypt a file");
        System.out.println("3. Break a cipher");
        System.out.println("4. Exit");
        System.out.print("Select an action: ");
    }


    /**
     * Запрашивает у пользователя путь к файлу в цикле, пока не будет введен
     * корректный путь. Позволяет отменить операцию, введя пустую строку.
     */
    private String promptForValidFilePath(String prompt) {
        while (true) {
            System.out.print(prompt);
            String path = scanner.nextLine();

            // Если пользователь нажал Enter - отмена.
            if (path.isBlank()) {
                System.out.println("Operation cancelled by user.");
                return null;
            }

            // Проверяем, существует ли файл.
            if (Files.exists(Paths.get(path))) {
                return path; // Путь корректный.
            } else {
                // Файл не найден, сообщаем об ошибке.
                System.out.println("File not found at the specified path. Please try again or press Enter to cancel.");
            }
        }
    }

    /**
     * Метод-обработчик для шифрования. Запрашивает все необходимые данные
     * (пути, ключ), вызывает сервисы для чтения и шифрования, сохраняет результат.
     */
    private void handleEncryption() {
        System.out.println("\n--- File Encryption ---");

        // Запрашиваем пути и ключ.
        String inputPath = promptForValidFilePath("Enter the path to the source file: ");
        if (inputPath == null) return; // Отмена.

        System.out.print("Enter the path to save the result: ");
        String outputPath = scanner.nextLine();
        if (outputPath.isBlank()) {
            System.out.println("Output path cannot be empty. Operation aborted.");
            return;
        }

        System.out.print("Enter the keyword: ");
        String key = scanner.nextLine();

        // Читаем, шифруем и записываем файл.
        String plainText = FileService.readFile(inputPath);
        if (plainText == null) return;

        String cipherText = vigenereCipher.encrypt(plainText, key);
        boolean success = FileService.writeFile(outputPath, cipherText);

        // Сообщаем результат.
        if (success) {
            System.out.println("File encrypted successfully and saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }

    /**
     * Метод-обработчик для расшифровки. Логика аналогична
     * шифрованию, но вызывает метод decrypt.
     */
    private void handleDecryption() {
        System.out.println("\n--- File Decryption ---");

        String inputPath = promptForValidFilePath("Enter the path to the encrypted file: ");
        if (inputPath == null) return;

        System.out.print("Enter the path to save the result: ");
        String outputPath = scanner.nextLine();
        if (outputPath.isBlank()) {
            System.out.println("Output path cannot be empty. Operation aborted.");
            return;
        }

        System.out.print("Enter the keyword: ");
        String key = scanner.nextLine();

        String cipherText = FileService.readFile(inputPath);
        if (cipherText == null) return;

        String plainText = vigenereCipher.decrypt(cipherText, key);
        boolean success = FileService.writeFile(outputPath, plainText);

        if (success) {
            System.out.println("File decrypted successfully and saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }

    /**
     * Метод-обработчик для взлома шифра. Вызывает VigenereBreaker
     * для выполнения криптоанализа.
     */
    private void handleBreakCipher() {
        System.out.println("\n--- Break Vigenere Cipher ---");

        String inputPath = promptForValidFilePath("Enter the path to the encrypted file: ");
        if (inputPath == null) return;

        System.out.print("Enter the path to save the result: ");
        String outputPath = scanner.nextLine();
        if (outputPath.isBlank()) {
            System.out.println("Output path cannot be empty. Operation aborted.");
            return;
        }

        String cipherText = FileService.readFile(inputPath);
        if (cipherText == null) return;

        // Определяем максимальную длину ключа для поиска и информируем пользователя.
        String preparedText = cipherText.replaceAll("[^a-zA-Z]", "");
        int maxKeyLength = VigenereBreaker.calculateMaxKeyLength(preparedText.length());

        if (maxKeyLength < VigenereBreaker.MIN_KEY_LENGTH) {
            System.out.println("Warning: The provided text is very short. Analysis will only be attempted for a Caesar cipher (key length 1). The result may be incorrect.");
        } else if (maxKeyLength == 1) {
            System.out.println("Note: The text is long enough only for a reliable Caesar cipher check (key length 1).");
        } else {
            System.out.println("Note: Based on the text length, the analyzer will search for keys from 1 up to " + maxKeyLength + " characters long.");
        }

        // Запускаем анализ и сохраняем результат.
        System.out.println("Starting analysis...");
        String plainText = vigenereBreaker.breakCipher(cipherText);

        boolean success = FileService.writeFile(outputPath, plainText);

        if (success) {
            System.out.println("Cipher broken successfully and result saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }
}