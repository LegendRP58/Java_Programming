package kursach;

    //Перечисление типов команд для обмена сообщениями между клиентом и сервером
    public enum CommandType {
    Ping(0),                      // Проверка соединения
    UserListUpdate(1),            // Обновление списка пользователей
    GetUserLocalIP(2),            // Запрос локального IP
    SendPrivateMessage(3),        // Личное сообщение
    SendBroadcastMessage(4),      // Сообщение всем
    SendFile(5),                  // Отправка файла
    Login(6),                     // Вход в систему
    Register(7);                  // Регистрация
    
    private final int value;
    
        //Конструктор enum
        CommandType(int value) {
        this.value = value;
    }
    
        //Геттер для получения числового значения команды
        public int getValue() {
        return value;
    }
    
        //Преобразование числа в CommandType
        public static CommandType fromValue(int value) {
        for (CommandType type : CommandType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неверный код команды: " + value);
    }
}