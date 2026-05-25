package kursach;

import java.io.Serializable;

    //Класс для представления сообщения в чате. Реализует Serializable для возможности сериализации
    public class Message implements Serializable {
    private static final long serialVersionUID = 1L;  // Для совместимости версий
    private static final String SEPARATOR = "|#|";     // Разделитель полей
    
    private CommandType commandType;  // Тип команды
    private String sender;             // Отправитель
    private String receiver;           // Получатель
    private String content;            // Содержимое сообщения
    
    // Конструктор по умолчанию (нужен для десериализации)
    public Message() {}
    
    // Конструктор с параметрами
    public Message(CommandType commandType, String sender, String receiver, String content) {
        this.commandType = commandType;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }
    
    //Сериализация сообщения в строку для передачи (Формат: "command|#|sender|#|receiver|#|content")
    public String serialize() {
        return commandType.getValue() + SEPARATOR + 
               (sender != null ? sender : "") + SEPARATOR + 
               (receiver != null ? receiver : "") + SEPARATOR + 
               (content != null ? content : "");
    }
    
        //Десериализация строки в объект Message

        public static Message deserialize(String data) throws ChatException {
        if (data == null || data.isEmpty()) {
            throw new ChatException("Пустые данные");
        }
        
        // Разделяем строку по разделителю
        String[] parts = data.split("\\|\\#\\|", -1);
        if (parts.length != 4) {
            throw new ChatException("Неверное количество частей: " + parts.length);
        }
        
        try {
            // Преобразуем первую часть в номер команды
            int commandInt = Integer.parseInt(parts[0]);
            CommandType commandType = CommandType.fromValue(commandInt);
            
            // Создаём и возвращаем сообщение
            return new Message(
                commandType,
                parts[1].isEmpty() ? null : parts[1],
                parts[2].isEmpty() ? null : parts[2],
                parts[3].isEmpty() ? null : parts[3]
            );
        } catch (IllegalArgumentException e) {
            throw new ChatException("Неверный код команды", e);
        }
    }
    
    // Геттеры и сеттеры
    public CommandType getCommandType() { return commandType; }
    public void setCommandType(CommandType commandType) { this.commandType = commandType; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}