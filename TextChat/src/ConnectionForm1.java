    import javax.swing.JOptionPane;
    import java.net.InetSocketAddress;
    import java.net.Socket;
    import java.io.IOException;
    
    //Форма подключения к серверу
    public class ConnectionForm1 extends javax.swing.JFrame {
    
    //Поля для хранения данных подключения
    private String serverIP;
    private int serverPort;

    //Конструктор формы
    public ConnectionForm1() {
        initComponents();
        txtIP.setText("");
        txtPort.setText("");
        btnConnect.setEnabled(false);   // Кнопка неактивна до валидного ввода
        
        //Окно по центру экрана
        this.setLocationRelativeTo(null);
        
        //Слушатели для валидации ввода
        txtIP.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
    });
    
        txtPort.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
    });
    }
    
    //Обновление состояния кнопки при вводе
    private void validateInputs() {
    boolean ipValid = validateIPAddress(txtIP.getText());
    boolean portValid = validatePort(txtPort.getText());
    btnConnect.setEnabled(ipValid && portValid);
}
    //Геттеры для получения данных после подключения
    public String getServerIP() {
        return serverIP;
    }
    public int getServerPort() {
        return serverPort;
    }
    
        //Метод проверки IP адреса
        private boolean validateIPAddress(String ip) {
        if (ip == null) {
            return false;
        }
        String trimmedIp = ip.trim();
        // Разрешаем только точное совпадение с 127.0.0.1 или localhost
        return "127.0.0.1".equals(trimmedIp) || "localhost".equalsIgnoreCase(trimmedIp);
    }

    //Метод проверки порта от 1 до 65535
    private boolean validatePort(String port) {
        if (port == null || port.trim().isEmpty()) {
            return false;
        }
        
        try {
            int portNumber = Integer.parseInt(port);
            return portNumber >= 1 && portNumber <= 65535;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtIP = new javax.swing.JTextField();
        txtPort = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("IP адрес:");

        jLabel2.setText("Порт:");

        txtIP.setText("setText");

        txtPort.setText("setText");

        btnConnect.setText("Подключиться");
        btnConnect.addActionListener(this::btnConnectActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPort)
                    .addComponent(txtIP))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(btnConnect)
                .addContainerGap(35, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnConnect)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

   
    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        String ip = txtIP.getText().trim();
        String portStr = txtPort.getText().trim();
    
        //Проверяем IP адрес
        if (!validateIPAddress(ip)) {
            JOptionPane.showMessageDialog(this, 
                "Неверный формат IP адреса", "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        //Проверяем порт
        if (!validatePort(portStr)) {
            JOptionPane.showMessageDialog(this, 
                "Порт должен быть числом от 1 до 65535", "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        //Сохраняем данные
        this.serverIP = ip;
        this.serverPort = Integer.parseInt(portStr);
   
        if (!checkServerAvailable(serverIP, serverPort)) {
        JOptionPane.showMessageDialog(this, 
            "Сервер недоступен!\nПроверьте IP, порт и запущен ли сервер.", 
            "Ошибка подключения", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }

        //Открываем главное окно чата
        ChatForm chatForm = new ChatForm(serverIP, serverPort);
        
    
        //Закрываем текущую форму
        this.dispose();
    }        
    private boolean checkServerAvailable(String ip, int port) {
    try (Socket testSocket = new Socket()) {
        testSocket.connect(new InetSocketAddress(ip, port), 3000); // Таймаут 3 секунды
        return true;
    } 
    catch (IOException e) {
        return false;
    }
    }
   public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> new ConnectionForm1().setVisible(true));
    }//GEN-LAST:event_btnConnectActionPerformed
      
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtIP;
    private javax.swing.JTextField txtPort;
    // End of variables declaration//GEN-END:variables
}