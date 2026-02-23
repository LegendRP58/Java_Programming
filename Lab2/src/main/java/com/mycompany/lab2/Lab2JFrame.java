package com.mycompany.lab2;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.util.List;

class RecIntegral { 
   // переменные таблицы
    private double step;
    private double upperLimit;
    private double lowerLimit;
    private double result;
     // конструктор по умолчанию
    public RecIntegral() {this.step = 0.0;
        this.upperLimit = 0.0;
        this.lowerLimit = 0.0;
        this.result = 0.0;
}
     // Конструктор с параметрами
    public RecIntegral(double step, double upperLimit, double lowerLimit, double result){
        this.step = step;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.result = result;
}
      // Конструктор без результата (для новых записей)
    public RecIntegral(double step, double upperLimit, double lowerLimit) {
        this.step = step;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.result = 0.0;
    }
    
    // геттеры и сеттеры переменных RecIntegral
    public double getStep() { return step;}
    public void setStep(double step) {this.step = step;}
    public double getUpperLimit() {return upperLimit;}
    public void setUpperLimit(double upperLimit) {this.upperLimit = upperLimit;} 
    public double getLowerLimit() {return lowerLimit;} 
    public void setLowerLimit(double lowerLimit) {this.lowerLimit = lowerLimit;}
    public double getResult() {return result;}
    public void setResult(double result) {this.result = result;}
    
        // Метод для преобразования в массив Object для таблицы
    public Object[] toTableRow() {
        return new Object[]{step, upperLimit, lowerLimit,result == 0.0 ? "" : String.format("%.5f", result)};
    }
}

public class Lab2JFrame extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Lab2JFrame.class.getName());
    private LinkedList<RecIntegral> recordsList;
    
    public Lab2JFrame() {
        initComponents();
        setupTableFormatting();
        recordsList = new LinkedList<>();
    }

      private double func(double x) {
    return Math.sin(x*x); 
    }
  
      private double computeIntegral(double step, double lowlim, double uplim) {
    if (lowlim >= uplim || step <= 0) {
        throw new IllegalArgumentException("Некорректные параметры: lowlim < uplim, step > 0");}
    
    double sum = 0.0;
    double x = lowlim;
    
    while (x < uplim) {
        //Подсчет шага с учетом погрешности с помощью минимизации Math.min
        double currentstep = Math.min(step, uplim - x);
        double nextX = x + currentstep;
        
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
        jPanel2 = new javax.swing.JPanel();
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
        ClearTableButton = new javax.swing.JButton();
        FillTableButton = new javax.swing.JButton();

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

        ClearTableButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        ClearTableButton.setText("Clear Table");
        ClearTableButton.addActionListener(this::ClearTableButtonActionPerformed);

        FillTableButton.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        FillTableButton.setText("Fill table");
        FillTableButton.addActionListener(this::FillTableButtonActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(DownLimitLabel)
                            .addComponent(StepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(LowerLimitTextField)
                            .addComponent(StepTextField)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(UpLimitLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(UpperLimitTextField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 71, Short.MAX_VALUE)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ExitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ClearStringButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ClearTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(AddToTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                            .addComponent(FillTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(32, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(StepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DownLimitLabel)
                    .addComponent(LowerLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UpLimitLabel)
                    .addComponent(UpperLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(AddToTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(ClearStringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(CountButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(ExitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FillTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ClearTableButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(43, 43, 43))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
            if (step == null || uplim == null || lowlim == null) { JOptionPane.showMessageDialog(this, "Некоторые значения отсутствуют", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Проверка корректности параметров
            if (lowlim >= uplim) {
                JOptionPane.showMessageDialog(this, "Нижний предел должен быть меньше верхнего предела", "Ошибка параметров", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (step <= 0) {JOptionPane.showMessageDialog(this, "Шаг должен быть положительным числом", "Ошибка параметров", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double result = computeIntegral(step, lowlim, uplim);
            
            // обновляем результат в коллекции
               if (selectedRow < recordsList.size()) {
                   RecIntegral record = recordsList.get(selectedRow);
                   record.setResult(result);
               }
               
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

    private void AddToTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddToTableButtonActionPerformed
        try {
            String stepText = StepTextField.getText().trim().replace(',', '.');  // Поддержка запятой
            String lowlimText = LowerLimitTextField.getText().trim().replace(',', '.');
            String uplimText = UpperLimitTextField.getText().trim().replace(',', '.');

             // проверка на заполнение полей ввода
        if (stepText.isEmpty() || lowlimText.isEmpty() || uplimText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните все поля", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }   
            double step = Double.parseDouble(stepText);
            double lowlim = Double.parseDouble(lowlimText);
            double uplim = Double.parseDouble(uplimText);

            if (lowlim >= uplim) {
                JOptionPane.showMessageDialog(this, "Нижний предел должен быть меньше верхнего предела", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (step <= 0) {JOptionPane.showMessageDialog(this, "Шаг должен быть положительным числом", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

         // объявление объекта для списка
         RecIntegral record = new RecIntegral(step, uplim, lowlim);
         // добавляем в список
         recordsList.add(record);
         // добавление в таюлицу
         DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
         model.addRow(record.toTableRow());
            // Очищаем поля
            StepTextField.setText("");
            LowerLimitTextField.setText("");
            UpperLimitTextField.setText("");
        }
        catch (NumberFormatException e) {JOptionPane.showMessageDialog(this, "Введите корректные числовые значения (например: 0.1, 0, 1)", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);}
    }//GEN-LAST:event_AddToTableButtonActionPerformed

    private void StepTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StepTextFieldActionPerformed

    }//GEN-LAST:event_StepTextFieldActionPerformed

    private void UpperLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpperLimitTextFieldActionPerformed

    }//GEN-LAST:event_UpperLimitTextFieldActionPerformed

    private void LowerLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LowerLimitTextFieldActionPerformed

    }//GEN-LAST:event_LowerLimitTextFieldActionPerformed

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitButtonActionPerformed
        System.exit(0);
    }//GEN-LAST:event_ExitButtonActionPerformed

    private void ClearTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearTableButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        
        int rowCount = model.getRowCount();
    for (int i = rowCount - 1; i >= 0; i--) {model.removeRow(i);}
    }//GEN-LAST:event_ClearTableButtonActionPerformed

    private void FillTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FillTableButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        
         if (recordsList.isEmpty()) {JOptionPane.showMessageDialog(this, "Коллекция пуста. Сначала добавьте данные", "Информация", JOptionPane.INFORMATION_MESSAGE);
        return;}
        
        int rowCount = model.getRowCount();
        for (int i = rowCount - 1; i >= 0; i--) {
            model.removeRow(i);
        }
        for (RecIntegral record : recordsList) {
            model.addRow(record.toTableRow());
        }
    }//GEN-LAST:event_FillTableButtonActionPerformed

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
         java.awt.EventQueue.invokeLater(() -> new Lab2JFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddToTableButton;
    private javax.swing.JButton ClearStringButton;
    private javax.swing.JButton ClearTableButton;
    private javax.swing.JButton CountButton;
    private javax.swing.JLabel DownLimitLabel;
    private javax.swing.JButton ExitButton;
    private javax.swing.JButton FillTableButton;
    private javax.swing.JTextField LowerLimitTextField;
    private javax.swing.JLabel StepLabel;
    private javax.swing.JTextField StepTextField;
    private javax.swing.JTable TableModel;
    private javax.swing.JLabel UpLimitLabel;
    private javax.swing.JTextField UpperLimitTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
