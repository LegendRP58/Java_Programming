package kursach;

//Исключение для обработки ошибок в чате (наследуется от exception (checked exception))
public class ChatException extends Exception {
    
    //Конструктор с сообщением об ошибке
    public ChatException(String message) {
        super(message);
    }
    
    // Конструктор с сообщением и причиной исключения
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}