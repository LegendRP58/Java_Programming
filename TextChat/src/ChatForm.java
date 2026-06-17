import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import kursach.*;

public class ChatForm extends javax.swing.JFrame {
    
    //Параметры подключения
    private String serverIP;
    private int serverPort;
    
    //Данные пользователя
    private String currentUser = null;
    private String currentPassword = null;
    private String selectedUser = "";
    private String localIP = "";
    
    // Список для хранения истории ключей сообщений во избежание дублирования
    private final java.util.List<String> receivedMessagesKeys = new java.util.ArrayList<>();
    
    //Флаги состояния
    private boolean acceptReg = false;
    private boolean shouldContinue = false;
    
    //Сетевые компоненты
    private Socket tcpClient;
    private Thread clientThread;
    
    //Таймеры
    private javax.swing.Timer pingTimer;
    private javax.swing.Timer updateTimer;
    private static final int UPLOAD_LIST_INTERVAL = 3000;
    
    // Защита от дубликатов на уровне класса 
    private static final java.util.List<String> globalReceivedKeys = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    
    //Директории
    private String logDirectory;
    private String filesDirectory;
    
    //Ссылка на форму авторизации
    private AuthForm authForm;

    //Конструктор с параметрами подключения
    public ChatForm(String ip, int port) {
        this.serverIP = ip;
        this.serverPort = port;
        
        
        //Инициализация директорий
        String userDir = System.getProperty("user.dir");
        logDirectory = userDir + File.separator + "log";
        filesDirectory = userDir + File.separator + "Files";
        new File(logDirectory).mkdirs();
        new File(filesDirectory).mkdirs();
        
        initComponents();
        //Очищаем поле ввода
        txtMessageInput.setText("");
        //Сначала  форма авторизации
        showAuthForm();
    }
    
   
    private void showAuthForm() {
        authForm = new AuthForm(this);
        authForm.setVisible(true);
    }
    
    //Установка данных логина/пароля из AuthForm
    public void setLoginData(String login, String password, String action) {
         this.currentUser = login;
    this.currentPassword = password;  
    
  
    try {
        connectToServer();
        startTimers();
        
        //Задержка, чтобы получить localIP
        Thread.sleep(500);
        getUserLocalIPRequest();
        
        if ("login".equals(action)) {
            loginUser(password);
        } else {
            registerUser(login, password);
        }
    } catch (Exception ex) {
       
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, 
            "Ошибка подключения: " + ex.getMessage(), "Ошибка", 
            JOptionPane.ERROR_MESSAGE);
        enableAuthFormButtons();
    }
    }
    
     public void closeAuthForm() {
        if (authForm != null) {
            authForm.dispose();
        }
    }
    
    //Разблокировать кнопки в AuthForm
    public void enableAuthFormButtons() {
        if (authForm != null) {
            authForm.enableButtons();
        }
    }
    
    //Подключение к серверу
    private void connectToServer() throws IOException {
        //Если поток уже запущен или сокет открыт — останавливаем их
        shouldContinue = false;
        if (tcpClient != null && !tcpClient.isClosed()) {
            try { tcpClient.close(); } catch (IOException ignored) {}
        }
        if (clientThread != null && clientThread.isAlive()) {
            clientThread.interrupt();
        }

        // Теперь создаем чистое новое подключение
        tcpClient = new Socket();
        tcpClient.connect(new InetSocketAddress(serverIP, serverPort), 5000);
        shouldContinue = true; 
        
        // Запуск ОДНОГО потока для чтения сообщений
        clientThread = new Thread(this::listenForMessages);
        clientThread.setDaemon(true);
        clientThread.start();
    }
    
    //Отправка сообщения на сервер
    private void sendMessage(CommandType commandType, String sender, String receiver, String content) {
        if (tcpClient == null || tcpClient.isClosed()) return;
        
        try {
            Message msg = new Message(commandType, sender, receiver, content);
            String data = msg.serialize();
            
            if (data.length() > 65500) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Сообщение слишком большое"));
                return;
            }
            
            OutputStream out = tcpClient.getOutputStream();
            
            out.write((data + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
            
        } catch (IOException ex) {
            System.err.println("Не удалось отправить сообщение: " + ex.getMessage());
        }
    }
    
    
    private void loginUser(String password) {
        sendMessage(CommandType.Login, localIP, "", currentUser + "," + password);
    }
    
    private void registerUser(String login, String password) {
        sendMessage(CommandType.Register, localIP, "", login + "," + password);
    }
    
    private void getUserLocalIPRequest() {
        sendMessage(CommandType.GetUserLocalIP, "", "", "");
    }
    
    private void listenForMessages() {
        
    try {
        InputStream in = tcpClient.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8));
        
        
        
        String line;
        while (shouldContinue && (line = reader.readLine()) != null) {
            
            try {
                Message msg = Message.deserialize(line);
                processCommand(msg);
            } catch (Exception ex) {
                System.err.println("Ошибка десериализации: " + ex.getMessage());
            }
        }
        
    } catch (IOException ex) {
        if (shouldContinue) {
            System.err.println("Соединение с сервером потеряно: " + ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Соединение с сервером потеряно.", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    }
    
    private void processCommand(Message msg) {
        SwingUtilities.invokeLater(() -> {
            try {
                switch (msg.getCommandType()) {
                    case UserListUpdate:
                        updateUserList(msg.getContent());
                        break;
                    case GetUserLocalIP:
                        localIP = msg.getContent();
                        System.out.println("Мой локальный IP: " + localIP);
                        break;
                    case SendPrivateMessage:
                        handlePrivateMessage(msg);
                        break;
                    case SendBroadcastMessage:
                        handleBroadcastMessage(msg);
                        break;
                    case SendFile:
                        createFile(msg.getSender(), msg.getContent());
                        break;
                    case Register:
                        checkRegister(msg.getContent());
                        break;
                    case Login:
                        checkLogin(msg.getContent());
                        break;
                    case Ping:
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    
    private void handlePrivateMessage(Message msg) {
        String dialogFile = getDialogFileName(msg.getSender(), currentUser);
        String messageToRich = "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + msg.getSender() + ": " + msg.getContent();
        saveMessageToLog(dialogFile, messageToRich);
        
        //Показываем сообщение, только если открыт диалог с этим пользователем
        if (selectedUser != null && selectedUser.equals(msg.getSender())) {
            txtChatLog.append(messageToRich + "\n");
        }
    }
    
    private void handleBroadcastMessage(Message msg) {
    // 1. Уровень защиты: Проверка по уникальному ключу (Отправитель + Текст)
        String messageKey = msg.getSender() + ":::" + msg.getContent().trim();
        
        if (globalReceivedKeys.contains(messageKey)) {
            return; // Если такое сообщение уже обрабатывалось — игнорируем дубликат
        }
        
        // Добавляем ключ в историю и держим размер кэша в пределах 100 записей
        globalReceivedKeys.add(messageKey);
        if (globalReceivedKeys.size() > 100) {
            globalReceivedKeys.remove(0);
        }
        
        // Форматируем строку для вывода
        String messageToRich = "[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + 
                               "] [Всем] " + msg.getSender() + ": " + msg.getContent();
        
        // 2. Уровень защиты: Проверяем, нет ли уже точно такой же строки на экране
        String currentChatText = txtChatLog.getText();
        if (currentChatText.endsWith(messageToRich + "\n") || currentChatText.contains(messageToRich)) {
            return; 
        }
        
        // 3. Уровень защиты: Сохраняем в файл лога только уникальное сообщение
        saveMessageToLog("ALL", messageToRich);
        
        // Выводим текст на экран
        txtChatLog.append(messageToRich + "\n");
    }
    
    private void checkLogin(String content) {
    String response = content.trim();
    acceptReg = false;
    
    if ("True".equals(response)) {
        acceptReg = true;
        lbCurrentUser.setText("Вы: " + currentUser);
        setTitle("Чат - " + currentUser);
        
        SwingUtilities.invokeLater(() -> {
            this.setLocationRelativeTo(null);
            this.setVisible(true);
            this.toFront();
        });
        closeAuthForm();
        sendMessage(CommandType.UserListUpdate, "", "", "");
    } else {
       
        JOptionPane.showMessageDialog(this, 
            "Неверный логин/пароль или пользователь в сети", 
            "Ошибка входа", JOptionPane.ERROR_MESSAGE);
        enableAuthFormButtons();
    }
}

private void checkRegister(String content) {
    
    
    if ("False".equals(content.trim())) {
        acceptReg = false;
        
        JOptionPane.showMessageDialog(this, "Такой логин уже существует");
        enableAuthFormButtons();
    } else if ("True".equals(content.trim())) {
        acceptReg = true;
        
        
        //Небольшая задержка перед авто-входом
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                
                loginUser(currentPassword);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    }
    
    private void updateUserList(String userList) {
    if (acceptReg && userList != null && !userList.isEmpty()) {
        //Сохраняем текущий выбранный элемент
        String previouslySelected = lstUsers.getSelectedValue();
        String[] userArray = userList.split(",");
        DefaultListModel<String> model = new DefaultListModel<>();
        
        //Элемент для общего чата в начало списка
        model.addElement("Общий чат");
        
        //Используем Stream API
        Arrays.stream(userArray)
            .filter(user -> !user.isEmpty() && !user.equals(currentUser))
            .forEach(model::addElement);
            
        lstUsers.setModel(model);
        
        //Восстанавливаем выбор, если пользователь всё ещё в списке
        if (previouslySelected != null && model.contains(previouslySelected)) {
            lstUsers.setSelectedValue(previouslySelected, true);
            selectedUser = previouslySelected;
        } else {
            // Если ничего не выбрано или пользователь вышел — выбираем общий чат
            lstUsers.setSelectedIndex(0);
            selectedUser = "Общий чат"; 
        }
    }
}
    
    private String getDialogFileName(String user1, String user2) {
        // Проверка на null
    String u1 = (user1 != null) ? user1 : "unknown";
    String u2 = (user2 != null) ? user2 : "unknown";
    
    // Сравниваем и возвращаем в алфавитном порядке
    if (u1.compareTo(u2) < 0) {
        return u1 + "_" + u2;
    } else {
        return u2 + "_" + u1;
    }
    }
    
    private void saveMessageToLog(String dialogName, String message) {
        String filePath = logDirectory + File.separator + dialogName + "_chatlog.txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            writer.println(message);
        } catch (IOException ex) {
            System.err.println("Ошибка записи в лог: " + ex.getMessage());
        }
    }
    
    private void createFile(String sender, String info) {
        try {
        String[] parts = info.split("\\|", 2);
        if (parts.length < 2) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка: неверный формат файла", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String fileName = parts[0].trim();
        String fileDataBase64 = parts[1].trim();
        
        // Создаём файл в папке Files
        String destPath = filesDirectory + File.separator + fileName;
        byte[] fileData = Base64.getDecoder().decode(fileDataBase64);
        Files.write(Paths.get(destPath), fileData);
        
        String message = "[" + LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")) + 
            "] Файл " + fileName + " получен от " + sender;
        
        txtChatLog.append(message + "\n");
        
        //Проверка на null
        if (currentUser != null && sender != null) {
            String dialogFile = getDialogFileName(sender, currentUser);
            saveMessageToLog(dialogFile, "[Файл] " + message);
        }
        
        JOptionPane.showMessageDialog(this, 
            "Файл получен: " + fileName + "\nСохранён в: " + destPath, 
            "Файл получен", JOptionPane.INFORMATION_MESSAGE);
            
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "Ошибка при создании файла: " + ex.getMessage(), 
            "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
    }
    
    private void startTimers() {
        pingTimer = new javax.swing.Timer(5000, e -> sendMessage(CommandType.Ping, "", "", ""));
        pingTimer.start();
        
        updateTimer = new javax.swing.Timer(UPLOAD_LIST_INTERVAL, e -> sendMessage(CommandType.UserListUpdate, "", "", ""));
        updateTimer.start();
    }
    
   private void transferFile(String receiver, String filePath) {
   
    try {
        // Проверки
        if (receiver == null || receiver.isEmpty()) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, 
                    "Ошибка: не выбран получатель!", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE));
            return;
        }
        
        if (currentUser == null) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, 
                    "Ошибка: вы не авторизованы!", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE));
            return;
        }
        
        // Читаем файл
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        String fileName = new File(filePath).getName();
        
       
        
        // Кодируем в Base64
        String fileInfo = fileName + "|" + Base64.getEncoder().encodeToString(fileData);
        
       
        
        // Проверяем размер (макс ~65KB в Base64)
        if (fileInfo.length() > 65000) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(this, 
                    "Файл слишком большой! Максимальный размер ~48KB", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE));
            return;
        }
        
        //Отправляем файл
 
        sendMessage(CommandType.SendFile, currentUser, receiver, fileInfo);
        
        //Показываем сообщение в своём чате
        String message = "[" + LocalTime.now().format(
            DateTimeFormatter.ofPattern("HH:mm:ss")) + 
            "] Файл \"" + fileName + "\" отправлен → " + receiver;
        
        SwingUtilities.invokeLater(() -> {
            txtChatLog.append(message + "\n");
        });
        
        //Сохраняем в лог
        String dialogFile = getDialogFileName(currentUser, receiver);
        saveMessageToLog(dialogFile, "[Файл] " + message);
        
        //Уведомление
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, 
                "📤 ФАЙЛ ОТПРАВЛЕН!\n\nИмя: " + fileName + 
                "\nКому: " + receiver + 
                "\nРазмер: " + fileData.length + " байт", 
                "Файл отправлен", JOptionPane.INFORMATION_MESSAGE));
                
    } catch (Exception ex) {
        
        ex.printStackTrace();
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, 
                "Ошибка при отправке файла:\n" + ex.getMessage(), 
                "Ошибка", JOptionPane.ERROR_MESSAGE));
    }
}
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        lstUsers = new javax.swing.JList<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtChatLog = new javax.swing.JTextArea();
        txtMessageInput = new javax.swing.JTextField();
        btnSendPrivate = new javax.swing.JButton();
        btnSendAll = new javax.swing.JButton();
        btnSendFile = new javax.swing.JButton();
        btnOpenFolder = new javax.swing.JButton();
        lbCurrentUser = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lstUsers.addListSelectionListener(this::lstUsersValueChanged);
        jScrollPane2.setViewportView(lstUsers);

        txtChatLog.setEditable(false);
        txtChatLog.setColumns(20);
        txtChatLog.setRows(5);
        jScrollPane1.setViewportView(txtChatLog);

        txtMessageInput.setText("jTextField1");

        btnSendPrivate.setText("Отправить лично");
        btnSendPrivate.addActionListener(this::btnSendPrivateActionPerformed);

        btnSendAll.setText("Отправить всем");
        btnSendAll.addActionListener(this::btnSendAllActionPerformed);

        btnSendFile.setText("Отправить файл");
        btnSendFile.addActionListener(this::btnSendFileActionPerformed);

        btnOpenFolder.setText("Загрузки");
        btnOpenFolder.addActionListener(this::btnOpenFolderActionPerformed);

        lbCurrentUser.setText("Вы:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbCurrentUser)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSendPrivate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSendFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenFolder)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addComponent(txtMessageInput))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lbCurrentUser)
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMessageInput, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSendPrivate)
                            .addComponent(btnSendAll)
                            .addComponent(btnSendFile)
                            .addComponent(btnOpenFolder))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendPrivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendPrivateActionPerformed
          String message = txtMessageInput.getText().trim();
    if (message.isEmpty()) return;
    
    //Получаем выбранного пользователя
    String selected = lstUsers.getSelectedValue();
    
    //Проверяем, не выбран ли "Общий чат"
    if (selected == null || selected.isEmpty() || selected.equals("Общий чат")) {
        JOptionPane.showMessageDialog(this,
            "Выберите пользователя из списка для личного сообщения!");
        return;
    }
    
    selectedUser = selected;
    sendMessage(CommandType.SendPrivateMessage, currentUser, selectedUser, message);
    txtMessageInput.setText("");
    }//GEN-LAST:event_btnSendPrivateActionPerformed

    private void btnSendAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendAllActionPerformed
       String message = txtMessageInput.getText().trim();
        if (message.isEmpty()) return;

        // Отправляем сообщение на сервер
        sendMessage(CommandType.SendBroadcastMessage, currentUser, "", message);

        // Очищаем поле ввода
        txtMessageInput.setText("");
    }//GEN-LAST:event_btnSendAllActionPerformed

    private void btnSendFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendFileActionPerformed
     //Получаем выбранного пользователя
    String selected = lstUsers.getSelectedValue();
    
    
    if (selected == null || selected.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "Выберите пользователя из списка!", 
            "Ошибка", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    if (selected == null || selected.isEmpty() || selected.equals("Общий чат")) {
    JOptionPane.showMessageDialog(this, 
        "Выберите конкретного пользователя для отправки файла!", 
        "Ошибка", JOptionPane.WARNING_MESSAGE);
    return;
}
    
    //Сохраняем выбранного пользователя
    selectedUser = selected;
    
    //Открываем диалог выбора файла
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Выберите файл для отправки");
    
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        
      
       
        //Запускаем отправку в отдельном потоке
        String receiver = selectedUser; // Сохраняем в final переменную
        Thread fileThread = new Thread(() -> 
            transferFile(receiver, selectedFile.getAbsolutePath()));
        fileThread.setDaemon(true);
        fileThread.start();
    }
    }//GEN-LAST:event_btnSendFileActionPerformed

    private void btnOpenFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenFolderActionPerformed
         try {
            new File(filesDirectory).mkdirs();
            Desktop.getDesktop().open(new File(filesDirectory));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при открытии папки: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnOpenFolderActionPerformed

    private void lstUsersValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstUsersValueChanged
        if (evt.getValueIsAdjusting()) {
        return;
    }

    String selected = lstUsers.getSelectedValue();
    
    // Проверяем, выбран ли "Общий чат"
    if (selected != null && selected.equals("Общий чат")) {
        selectedUser = null; // Сбрасываем выбранного пользователя
        txtChatLog.setText(""); // Показываем общий лог
        String filePath = logDirectory + File.separator + "ALL_chatlog.txt";
        try {
            if (new File(filePath).exists()) {
                String chatLog = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(filePath)), StandardCharsets.UTF_8);
                txtChatLog.setText(chatLog);
            }
        } catch (IOException ex) {
            System.err.println("Ошибка чтения общего лога: " + ex.getMessage());
        }
    } else if (selected != null) { // Выбран конкретный пользователь
        selectedUser = selected;
        txtChatLog.setText("");
        String dialogFile = getDialogFileName(currentUser, selectedUser);
        String filePath = logDirectory + File.separator + dialogFile + "_chatlog.txt";
        try {
            if (new File(filePath).exists()) {
                String chatLog = new String(java.nio.file.Files.readAllBytes(
                    java.nio.file.Paths.get(filePath)), StandardCharsets.UTF_8);
                txtChatLog.setText(chatLog);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка при чтении файла: " + ex.getMessage());
        }
    }
    }//GEN-LAST:event_lstUsersValueChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
          shouldContinue = false;
        if (pingTimer != null) pingTimer.stop();
        if (updateTimer != null) updateTimer.stop();
        if (tcpClient != null) {
            try { tcpClient.close(); } catch (IOException ignored) {}
        }
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnOpenFolder;
    private javax.swing.JButton btnSendAll;
    private javax.swing.JButton btnSendFile;
    private javax.swing.JButton btnSendPrivate;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lbCurrentUser;
    private javax.swing.JList<String> lstUsers;
    private javax.swing.JTextArea txtChatLog;
    private javax.swing.JTextField txtMessageInput;
    // End of variables declaration//GEN-END:variables
}