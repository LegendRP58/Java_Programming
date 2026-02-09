/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

package com.mycompany.lab1;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class Lab1JFrame extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Lab1JFrame.class.getName());
  
    public Lab1JFrame() {
        initComponents();
        setupTableFormatting();
};
  
    private double func(double x) {
    return Math.sin(x*x); 
    }
  
    private double computeIntegral(double step, double lowlim, double uplim) {
    if (lowlim >= uplim || step <= 0) {
        throw new IllegalArgumentException("Некорректные параметры: lowlim < uplim, step > 0");
    }
    double sum = 0.0;
    double x = lowlim;
    while (x < uplim) {
        double nextX = x + step;
        // Если следующая точка выходит за b — ограничиваем её значением b
        if (nextX > uplim) {
            nextX = uplim;
        }
        double y1 = func(x);
        double y2 = func(nextX);
        // Площадь трапеции
        sum += (y1 + y2) * (nextX - x) / 2.0;
        x = nextX;
    }
    return sum;
}
    private void setupTableFormatting() {
    // Устанавливаем рендерер для отображения чисел
    javax.swing.table.DefaultTableCellRenderer rightRenderer = new javax.swing.table.DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(javax.swing.JLabel.RIGHT);
    
    // Применяем рендерер ко всем столбцам
    for (int i = 0; i < TableModel.getColumnCount(); i++) {
        TableModel.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
    }
    
}
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableModel = new javax.swing.JTable();
        CountButton = new javax.swing.JButton();
        ClearStringButton = new javax.swing.JButton();
        AddToTableButton = new javax.swing.JButton();
        StepLabel = new javax.swing.JLabel();
        UpLimitLabel = new javax.swing.JLabel();
        DownLimitLabel = new javax.swing.JLabel();
        StepTextField = new javax.swing.JTextField();
        UpperLimitTextField = new javax.swing.JTextField();
        LowerLimitTextField = new javax.swing.JTextField();
        ExitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 3, 32)); // NOI18N
        jLabel1.setText("Function is sin(x²)");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        TableModel.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        TableModel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TableModel.setForeground(new java.awt.Color(153, 153, 153));
        TableModel.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Step", "Upper Limit", "Lower Limit", "Count Result"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableModel.setToolTipText("");
        TableModel.setShowVerticalLines(true);
        jScrollPane1.setViewportView(TableModel);

        CountButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        CountButton.setText("Count");
        CountButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CountButton.addActionListener(this::CountButtonActionPerformed);

        ClearStringButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        ClearStringButton.setText("Clear string");
        ClearStringButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ClearStringButton.addActionListener(this::ClearStringButtonActionPerformed);

        AddToTableButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        AddToTableButton.setText("Add to table");
        AddToTableButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        AddToTableButton.addActionListener(this::AddToTableButtonActionPerformed);

        StepLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        StepLabel.setText("Step:");

        UpLimitLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        UpLimitLabel.setText("Upper Limit:");

        DownLimitLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        DownLimitLabel.setText("Lower Limit:");

        StepTextField.addActionListener(this::StepTextFieldActionPerformed);

        UpperLimitTextField.addActionListener(this::UpperLimitTextFieldActionPerformed);

        LowerLimitTextField.addActionListener(this::LowerLimitTextFieldActionPerformed);

        ExitButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        ExitButton.setText("Exit");
        ExitButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ExitButton.addActionListener(this::ExitButtonActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(DownLimitLabel)
                            .addComponent(StepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LowerLimitTextField)
                            .addComponent(StepTextField)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(UpLimitLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(UpperLimitTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 72, Short.MAX_VALUE)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ExitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addComponent(ClearStringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(AddToTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(33, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(StepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DownLimitLabel)
                    .addComponent(LowerLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UpLimitLabel)
                    .addComponent(UpperLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(AddToTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ClearStringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(CountButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ExitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CountButtonActionPerformed
    int selectedRow = TableModel.getSelectedRow(); 
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Выберите строку для вычисления", "Информация", JOptionPane.WARNING_MESSAGE);
        return; 
    }
    try{
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        
        // Получаем значения из таблицы
        Double step = (Double) TableModel.getValueAt(selectedRow, 0);
        Double uplim = (Double) TableModel.getValueAt(selectedRow, 1);
        Double lowlim = (Double) TableModel.getValueAt(selectedRow, 2);
        
        // Проверяем на null
        if (step == null || uplim == null || lowlim == null) {
            JOptionPane.showMessageDialog(this, "Некоторые значения отсутствуют", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
     
        // Проверка корректности параметров
        if (lowlim >= uplim) {
            JOptionPane.showMessageDialog(this, 
                "Нижний предел должен быть меньше верхнего предела", 
                "Ошибка параметров", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (step <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Шаг должен быть положительным числом", 
                "Ошибка параметров", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        double result = computeIntegral(step, lowlim, uplim);
        
        // Форматируем результат до 5 знаков
        model.setValueAt(String.format("%.5f", result), selectedRow, 3);
    }
     catch(ClassCastException e) {
        // обработка неверного формата
        JOptionPane.showMessageDialog(this, "Некорректный формат данных в таблице", "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
    catch(Exception e){
        JOptionPane.showMessageDialog(this, "Ошибка при вычислении: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
    }//GEN-LAST:event_CountButtonActionPerformed

    private void ClearStringButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearStringButtonActionPerformed
        int selectedRow = TableModel.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Выберите строку для удаления", "Информация", JOptionPane.WARNING_MESSAGE);
        return;
    }
    // получаем модель и удаляем строку
    DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
    model.removeRow(selectedRow);
    }//GEN-LAST:event_ClearStringButtonActionPerformed

    private void UpperLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpperLimitTextFieldActionPerformed
    }//GEN-LAST:event_UpperLimitTextFieldActionPerformed

    private void LowerLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LowerLimitTextFieldActionPerformed
    }//GEN-LAST:event_LowerLimitTextFieldActionPerformed

    private void AddToTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddToTableButtonActionPerformed
           try {
        String stepText = StepTextField.getText().trim().replace(',', '.');  // Поддержка запятой
        String lowlimText = LowerLimitTextField.getText().trim().replace(',', '.');
        String uplimText = UpperLimitTextField.getText().trim().replace(',', '.');
        
        double step = Double.parseDouble(stepText);
        double lowlim = Double.parseDouble(lowlimText);
        double uplim = Double.parseDouble(uplimText);
        
        // Проверка
        if (lowlim >= uplim) {
            JOptionPane.showMessageDialog(this, 
                "Нижний предел должен быть меньше верхнего предела", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (step <= 0) {
            JOptionPane.showMessageDialog(this, 
                "Шаг должен быть положительным числом", 
                "Ошибка", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        
        model.addRow(new Object[]{step, uplim, lowlim, ""});
        
        // Очищаем поля
        StepTextField.setText("");
        LowerLimitTextField.setText("");
        UpperLimitTextField.setText("");
        
        StepTextField.requestFocus();  
    }
    catch (NumberFormatException e) {JOptionPane.showMessageDialog(this, "Введите корректные числовые значения (например: 0.1, 0, 1)", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);}
    }//GEN-LAST:event_AddToTableButtonActionPerformed

    private void StepTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StepTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StepTextFieldActionPerformed

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_ExitButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
       
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }   
        catch (Exception e) {
        }

        java.awt.EventQueue.invokeLater(() -> new Lab1JFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddToTableButton;
    private javax.swing.JButton ClearStringButton;
    private javax.swing.JButton CountButton;
    private javax.swing.JLabel DownLimitLabel;
    private javax.swing.JButton ExitButton;
    private javax.swing.JTextField LowerLimitTextField;
    private javax.swing.JLabel StepLabel;
    private javax.swing.JTextField StepTextField;
    private javax.swing.JTable TableModel;
    private javax.swing.JLabel UpLimitLabel;
    private javax.swing.JTextField UpperLimitTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
