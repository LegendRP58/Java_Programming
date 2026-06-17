import javax.swing.*;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

//Форма авторизации
public class AuthForm extends javax.swing.JFrame {
    private ChatForm parentForm; // Ссылка на родительскую форму чата

    //Конструктор формы
    public AuthForm(ChatForm parent) {
        initComponents();
        this.parentForm = parent;
        
        //Очищаем поля при запуске
        txtLogin.setText("");
        txtPassword.setText("");
        
        //Отключаем кнопки по умолчанию
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        
        //Центрируем окно
        this.setLocationRelativeTo(null);
        
        //Слушатели для валидации логина и пароля
        txtLogin.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        });
        
        txtPassword.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateInputs(); }
        });
    
    }

    //Проверка логина: 3-20 символов, только буквы, цифры, подчёркивания
    private boolean validateLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        if (login.length() < 3 || login.length() > 20) {
            return false;
        }
        // Регулярное выражение: только латиница, цифры, подчёркивания
        return Pattern.matches("^[a-zA-Z0-9_]+$", login);
    }

    //Проверка пароля: 6-30 символов
     private boolean validatePassword(String password) {
        if (password == null) {
            return false;
        }
        String trimmedPassword = password.trim();
        if (trimmedPassword.isEmpty()) {
            return false;
        }
        return trimmedPassword.length() >= 6 && trimmedPassword.length() <= 30;
    }

    //Обновление состояния кнопок при вводе
    private void validateInputs() {
        boolean loginValid = validateLogin(txtLogin.getText());
        boolean passValid = validatePassword(new String(txtPassword.getPassword()));
        btnLogin.setEnabled(loginValid && passValid);
        btnRegister.setEnabled(loginValid && passValid);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtLogin = new javax.swing.JTextField();
        btnLogin = new javax.swing.JButton();
        btnRegister = new javax.swing.JButton();
        txtPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("Логин:");

        jLabel2.setText("Пароль:");
        jLabel2.setToolTipText("");

        txtLogin.setText("jTextField1");
        txtLogin.addActionListener(this::txtLoginActionPerformed);

        btnLogin.setText("Войти");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        btnRegister.setText("Регистрация");
        btnRegister.addActionListener(this::btnRegisterActionPerformed);

        txtPassword.setText("jPasswordField1");
        txtPassword.setEchoChar('*');
        txtPassword.addActionListener(this::txtPasswordActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnRegister, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtLogin)
                            .addComponent(txtPassword))))
                .addGap(62, 62, 62))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLogin)
                    .addComponent(btnRegister))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
    String login = txtLogin.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (!validateLogin(login)) {
            JOptionPane.showMessageDialog(this, 
                "Логин: 3-20 символов (буквы, цифры, _)", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validatePassword(password)) {
            JOptionPane.showMessageDialog(this, 
                "Пароль: 6-30 символов", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //Блокируем кнопки
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        
        //Передаём данные в ChatForm для регистрации
        if (parentForm != null) {
            parentForm.setLoginData(login, password, "register");
        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
    String login = txtLogin.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        if (!validateLogin(login)) {
            JOptionPane.showMessageDialog(this, 
                "Логин: 3-20 символов (буквы, цифры, _)", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!validatePassword(password)) {
            JOptionPane.showMessageDialog(this, 
                "Пароль: 6-30 символов", 
                "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //Блокируем кнопки на время обработки
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        
        //Передаём данные в ChatForm для входа
        if (parentForm != null) {
            parentForm.setLoginData(login, password, "login");
        }  
        
    
    }//GEN-LAST:event_btnLoginActionPerformed

    private void txtLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLoginActionPerformed
        validateInputs();
    }//GEN-LAST:event_txtLoginActionPerformed

    private void txtPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPasswordActionPerformed
        validateInputs();
    }//GEN-LAST:event_txtPasswordActionPerformed
    
    //Разблокировать кнопки (вызывается при неудачной авторизации)
    public void enableButtons() {
           if (SwingUtilities.isEventDispatchThread()) {
               btnLogin.setEnabled(true);
               btnRegister.setEnabled(true);
           } else {
               SwingUtilities.invokeLater(() -> enableButtons());
           }
       }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField txtLogin;
    private javax.swing.JPasswordField txtPassword;
    // End of variables declaration//GEN-END:variables
}
