package ui;

import analysis.VigenereBreaker;
import cipher.VigenereCipher;
import services.FileService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Класс, отвечающий за взаимодействие с пользователем через консоль.
 * Он управляет основным циклом программы, отображает меню, обрабатывает
 * ввод пользователя и координирует вызовы других компонентов
 * (шифратора, взломщика, файлового сервиса).
 */
public class ConsoleUI {

    // Поля класса для хранения экземпляров ключевых компонентов:
    // Scanner для чтения ввода, VigenereCipher для шифрования/дешифрования,
    // и VigenereBreaker для выполнения криптоанализа.
    private final Scanner scanner;
    private final VigenereCipher vigenereCipher;
    private final VigenereBreaker vigenereBreaker;

    // Конструктор класса. Инициализирует все необходимые объекты при создании экземпляра ConsoleUI.
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.vigenereCipher = new VigenereCipher();
        this.vigenereBreaker = new VigenereBreaker();
    }

    /**
     * Основной метод, запускающий главный цикл программы. В цикле отображается
     * меню, считывается выбор пользователя и вызывается соответствующий
     * метод-обработчик. Цикл завершается, когда пользователь выбирает опцию выхода.
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
                    return; // Выход из метода run() и завершение программы
                default:
                    System.out.println("Invalid input. Please choose an option from 1 to 4.");
                    break;
            }
            // Пауза, чтобы пользователь успел прочитать результат перед возвратом в меню.
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    /**
     * Вспомогательный метод, который выводит в консоль главное меню программы.
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
     * Вспомогательный метод для улучшения пользовательского опыта. Он запрашивает
     * у пользователя путь к файлу в цикле до тех пор, пока не будет введен
     * корректный путь к существующему файлу. Предоставляет пользователю
     * возможность отменить операцию, введя пустую строку.
     */
    private String promptForValidFilePath(String prompt) {
        while (true) {
            System.out.print(prompt);
            String path = scanner.nextLine();

            // Если пользователь ничего не ввел и нажал Enter - отмена.
            if (path.isBlank()) {
                System.out.println("Operation cancelled by user.");
                return null;
            }

            // Проверяем, существует ли файл по указанному пути.
            if (Files.exists(Paths.get(path))) {
                return path; // Путь корректный, возвращаем его.
            } else {
                // Файл не найден, сообщаем об ошибке и просим ввести снова.
                System.out.println("File not found at the specified path. Please try again or press Enter to cancel.");
            }
        }
    }

    /**
     * Метод-обработчик для опции шифрования. Он последовательно запрашивает у пользователя
     * все необходимые данные (пути к файлам, ключ), вызывает сервисы для чтения
     * файла и выполнения шифрования, а затем сохраняет результат.
     */
    private void handleEncryption() {
        System.out.println("\n--- File Encryption ---");

        String inputPath = promptForValidFilePath("Enter the path to the source file: ");
        if (inputPath == null) return; // Пользователь отменил ввод.

        System.out.print("Enter the path to save the result: ");
        String outputPath = scanner.nextLine();
        if (outputPath.isBlank()) {
            System.out.println("Output path cannot be empty. Operation aborted.");
            return;
        }

        System.out.print("Enter the keyword: ");
        String key = scanner.nextLine();

        String plainText = FileService.readFile(inputPath);
        if (plainText == null) {
            return;
        }

        String cipherText = vigenereCipher.encrypt(plainText, key);
        boolean success = FileService.writeFile(outputPath, cipherText);

        if (success) {
            System.out.println("File encrypted successfully and saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }

    /**
     * Метод-обработчик для опции расшифровки. Его логика аналогична
     * методу шифрования, но вызывает метод decrypt у объекта VigenereCipher.
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
        if (cipherText == null) {
            return;
        }

        String plainText = vigenereCipher.decrypt(cipherText, key);
        boolean success = FileService.writeFile(outputPath, plainText);

        if (success) {
            System.out.println("File decrypted successfully and saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }

    /**
     * Метод-обработчик для опции взлома шифра. Запрашивает пути к файлам
     * и вызывает метод breakCipher у объекта VigenereBreaker для выполнения
     * криптоанализа.
     */
    private void handleBreakCipher() {
        System.out.println("\n--- Break Vigenere Cipher ---");
        System.out.println("Note: The analyzer will search for keys up to 20 characters long.");

        String inputPath = promptForValidFilePath("Enter the path to the encrypted file: ");
        if (inputPath == null) return;

        System.out.print("Enter the path to save the result: ");
        String outputPath = scanner.nextLine();
        if (outputPath.isBlank()) {
            System.out.println("Output path cannot be empty. Operation aborted.");
            return;
        }

        String cipherText = FileService.readFile(inputPath);
        if (cipherText == null) {
            return;
        }

        String plainText = vigenereBreaker.breakCipher(cipherText);
        boolean success = FileService.writeFile(outputPath, plainText);

        if (success) {
            System.out.println("Cipher broken successfully and result saved to: " + outputPath);
        } else {
            System.out.println("Failed to save the file. Operation aborted.");
        }
    }
}