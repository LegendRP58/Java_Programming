package server;

import kursach.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

//Обработчик подключенного клиента (выполняется в отдельном потоке)
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String clientIP;
    private String login = null;
    private volatile boolean connected = true;
    private final UserManager userManager;
    private final List<ClientHandler> clients;
    // Статический кэш (общий для всех потоков клиентов) для отслеживания дубликатов на сервере
    private static final java.util.Set<String> serverReceivedCache = java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    public ClientHandler(Socket socket, String ip, UserManager userManager, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clientIP = ip;
        this.userManager = userManager;
        this.clients = clients;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while (connected && (line = reader.readLine()) != null) {
                try {
                    Message msg = Message.deserialize(line);
                    processMessage(msg);
                } catch (ChatException ex) {
                    System.err.println("Ошибка обработки сообщения от " + clientIP + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("Ошибка чтения от " + clientIP + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    //Отправка сообщения клиенту (с символом новой строки для readLine())
    public boolean sendMessage(Message msg) {
        if (!connected || clientSocket.isClosed()) {
            return false;
        }
        
        try {
            String data = msg.serialize() + "\n"; 
            OutputStream out = clientSocket.getOutputStream();
            out.write(data.getBytes(StandardCharsets.UTF_8));
            out.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка отправки клиенту " + clientIP + ": " + e.getMessage());
            disconnect();
            return false;
        }
    }

    //Отключение клиента
    public void disconnect() {
        if (connected) {
            connected = false;
            try { 
                clientSocket.close(); 
            } catch (IOException ignored) {}
            
            System.out.println("Клиент отключён: " + clientIP + " (логин: " + login + ")");
            
            synchronized (clients) {
                clients.remove(this);
            }
            
            //Рассылаем обновлённый список пользователей всем
            broadcastUserList();
        }
    }

    //Обработка сообщения по типу команды
    private void processMessage(Message msg) {
        switch (msg.getCommandType()) {
            case Ping:
                handlePing();
                break;
            case UserListUpdate:
                handleUserListUpdate();
                break;
            case GetUserLocalIP:
                handleGetLocalIP();
                break;
            case Login:
                handleLogin(msg);
                break;
            case Register:
                handleRegister(msg);
                break;
            case SendPrivateMessage:
                handlePrivateMessage(msg);
                break;
            case SendBroadcastMessage:
                handleBroadcastMessage(msg);
                break;
            case SendFile:
                handleFileTransfer(msg);
                break;
        }
    }

    //Обработка Ping - просто отвечаем таким же Ping
    private void handlePing() {
        sendMessage(new Message(CommandType.Ping, "", "", ""));
    }

    //Отправка списка пользователей текущему клиенту
    private void handleUserListUpdate() {
        List<String> allUsers = userManager.getAllLogins();
        String userList = String.join(",", allUsers);
        sendMessage(new Message(CommandType.UserListUpdate, "", "", userList));
    }

    //Отправка клиенту его локального IP
    private void handleGetLocalIP() {
        sendMessage(new Message(CommandType.GetUserLocalIP, "", "", clientIP));
    }

    //Обработка входа в систему
    private void handleLogin(Message msg) {
         String[] creds = msg.getContent().split(",", 2);
        if (creds.length != 2) {
            sendLoginResponse(false);
            return;
        }
        String loginAttempt = creds[0];
        String passwordAttempt = creds[1];

        //Проверяем корректность логина и пароля
        if (!userManager.validateLogin(loginAttempt, passwordAttempt)) {
            sendLoginResponse(false);
            System.out.println("Неудачная попытка входа: " + loginAttempt + " с IP: " + clientIP);
            return;
        }

        //Проверяем не занят ли логин другим активным клиентом
        boolean isAlreadyLoggedIn = false;
        synchronized (clients) {
            for (ClientHandler client : clients) {
                //Исключаем текущего клиента, проверяем активность и совпадение логина
                if (client != this && client.connected && client.login != null && client.login.equals(loginAttempt)) {
                    isAlreadyLoggedIn = true;
                    break;
                }
            }
        }

        if (isAlreadyLoggedIn) {
            sendLoginResponse(false);
            System.out.println("Вход отклонён: пользователь '" + loginAttempt + "' уже в сети с другого устройства.");
            return;
        }

        //Успешная авторизация
        this.login = loginAttempt;
        sendLoginResponse(true);
        System.out.println("Пользователь '" + login + "' вошёл с IP: " + clientIP);
        broadcastUserList();  // Обновляем списки всех пользователей
    }

    //Обработка регистрации нового пользователя
    private void handleRegister(Message msg) {
        String[] creds = msg.getContent().split(",", 2);
        
        if (creds.length != 2) {
            sendRegisterResponse(false);
            return;
        }
        
        String newLogin = creds[0];
        String newPassword = creds[1];
        
        if (userManager.registerUser(newLogin, newPassword)) {
            sendRegisterResponse(true);
            System.out.println("Зарегистрирован новый пользователь: " + newLogin);
        } else {
            sendRegisterResponse(false);
            System.out.println("Ошибка регистрации (логин уже существует): " + newLogin);
        }
    }

    //Пересылка личного сообщения указанному получателю
    private void handlePrivateMessage(Message msg) {
        boolean delivered = false;
        
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.login != null && 
                    client.login.equals(msg.getReceiver()) && 
                    client.connected) {
                    
                    Message forward = new Message(
                        CommandType.SendPrivateMessage,
                        msg.getSender(),
                        msg.getReceiver(),
                        msg.getContent()
                    );
                    client.sendMessage(forward);
                    delivered = true;
                    break;
                }
            }
        }
        
        if (!delivered) {
            System.out.println("Не удалось доставить личное сообщение пользователю: " + msg.getReceiver());
        }
    }

    //Рассылка широковещательного сообщения всем 
    private void handleBroadcastMessage(Message msg) {
        // Создаем уникальный ключ пакета: "ИмяОтправителя:::ТекстСообщения"
    String serverKey = msg.getSender() + ":::" + msg.getContent();

    // Проверяем, не рассылали ли мы УЖЕ точно такое же сообщение от этого пользователя
    if (serverReceivedCache.contains(serverKey)) {
        return; // Пропускаем дубликат, не отправляем его в цикл по пользователям!
    }

    // Если сообщение новое — запоминаем его
    serverReceivedCache.add(serverKey);

    // Чтобы кэш не разрастался бесконечно, очищаем его, если в нем накопилось много записей
    if (serverReceivedCache.size() > 100) {
        serverReceivedCache.clear();
    }

    // Собственно сама рассылка (ваш исходный код, но теперь защищенный)
    synchronized (clients) {
        for (ClientHandler client : clients) {
            // Отправляем всем подключенным, кроме автора сообщения
            if (client != this && client.connected) {
                client.sendMessage(new Message(
                    CommandType.SendBroadcastMessage,
                    msg.getSender(),
                    "",
                    msg.getContent()
                ));
            }
        }
    }
    }

    //Пересылка файла указанному получателю
    private void handleFileTransfer(Message msg) {
    
    boolean delivered = false;
    
    synchronized (clients) {
        for (ClientHandler client : clients) {
          
            
            if (client.login != null && 
                client.login.equals(msg.getReceiver()) && 
                client.connected) {
                
      
                
                Message forward = new Message(
                    CommandType.SendFile,
                    msg.getSender(),
                    msg.getReceiver(),
                    msg.getContent()
                );
                
                boolean sent = client.sendMessage(forward);
               
                delivered = true;
                break;
            }
        }
    }
    
    if (!delivered) {
        
        synchronized (clients) {
            for (ClientHandler c : clients) {
            }
        }
    }
}

    //Отправка ответа на попытку входа
    private void sendLoginResponse(boolean success) {
        sendMessage(new Message(CommandType.Login, "", "", success ? "True" : "False"));
    }

    //Отправка ответа на попытку регистрации
    private void sendRegisterResponse(boolean success) {
        sendMessage(new Message(CommandType.Register, "", "", success ? "True" : "False"));
    }

    //Рассылка обновлённого списка пользователей всем подключенным клиентам
    private void broadcastUserList() {
        List<String> allUsers = userManager.getAllLogins();
        String userList = String.join(",", allUsers);
        
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client.connected) {
                    client.sendMessage(new Message(CommandType.UserListUpdate, "", "", userList));
                }
            }
        }
    }

    //Геттеры
    public String getLogin() { 
        return login; 
    }
    
    public boolean isConnected() { 
        return connected; 
    }
    
    public String getClientIP() {
        return clientIP;
    }
}