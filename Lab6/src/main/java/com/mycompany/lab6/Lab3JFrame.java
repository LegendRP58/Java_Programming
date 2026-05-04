package com.mycompany.lab6;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedList;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class Lab3JFrame extends javax.swing.JFrame {
    private static final int MAX_CLIENTS = 20; 
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Lab3JFrame.class.getName());
    private java.util.List<Process> clientProcesses = new java.util.ArrayList<>();
    private LinkedList<RecIntegral> recordsList;
     private boolean tcpClientsStarted = false;
    private boolean udpClientsStarted = false;
    private boolean computingInProgress = false; // флаг для блокировки повторного запуска
    private Thread tcpServerThread;
    private Thread udpServerThread;
    private ServerTCP tcpServer;
    private ServerUDP udpServer;
    private int tcpClientsCount = 0;
    private int udpClientsCount = 0;

    
    public Lab3JFrame() {
        initComponents();
        setupTableFormatting();
        setupTableListener();
        recordsList = new LinkedList<>();
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
    
private boolean isUpdating = false; // флаг для предотвращения рекурсивного вызова ошибки
    
private void setupTableListener() {
    DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
    
    model.addTableModelListener(e -> {
        if (isUpdating) return;
        
        if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int column = e.getColumn();
            
            if (row >= 0 && column >= 0 && column <= 2 && row < recordsList.size()) {
                isUpdating = true;
                try {
                    RecIntegral record = recordsList.get(row);
                    // Сохраняем старые значения для восстановления
                    double oldStep = record.getStep();
                    double oldUpper = record.getUpperLimit();
                    double oldLower = record.getLowerLimit();
                    
                    Object newValue = model.getValueAt(row, column);
                    if (newValue != null) {
                        String valueStr = newValue.toString().trim().replace(',', '.');
                        if (!valueStr.isEmpty()) {
                            double value = Double.parseDouble(valueStr);
                            
                            try {
                                // Валидация и обновление с использованием класса исключения
                                switch (column) {
                                    case 0: 
                                        record.setStep(value);
                                        break;
                                    case 1: 
                                        record.setUpperLimit(value);
                                        break;
                                    case 2: 
                                        record.setLowerLimit(value);
                                        break;
                                }
                                
                                // проверка соотношения пределов
                                if (record.getLowerLimit() >= record.getUpperLimit()) {
                                    JOptionPane.showMessageDialog(this, "Нижний предел должен быть меньше верхнего", "Ошибка", JOptionPane.WARNING_MESSAGE);
                                    
                                    // Восстанавливаем значения
                                    record.setStep(oldStep);
                                    record.setUpperLimit(oldUpper);
                                    record.setLowerLimit(oldLower);
                                    
                                    // Обновляем таблицу
                                    model.setValueAt(oldStep, row, 0);
                                    model.setValueAt(oldUpper, row, 1);
                                    model.setValueAt(oldLower, row, 2);
                                } else {
                                    record.setResult(0.0);
                                    model.setValueAt("", row, 3);
                                }
                                
                            } catch (IntegralException ex) {
                                // Обработка исключения при редактировании ячейки
                                String fieldName = "";
                                double restoreValue = 0;
                                
                                switch (column) {
                                    case 0: 
                                        fieldName = "Шаг";
                                        restoreValue = oldStep;
                                        break;
                                    case 1: 
                                        fieldName = "Верхний предел";
                                        restoreValue = oldUpper;
                                        break;
                                    case 2: 
                                        fieldName = "Нижний предел";
                                        restoreValue = oldLower;
                                        break;
                                }
                                
                                JOptionPane.showMessageDialog(this,String.format("Некорректное значение для поля '%s': %.6f\n\n" +"Значение должно быть в диапазоне от %.6f до %.6f",
                                        fieldName, value, RecIntegral.MIN_VALUE, RecIntegral.MAX_VALUE),"Ошибка валидации",JOptionPane.WARNING_MESSAGE);                        
                                // Восстанавливаем старое значение
                                model.setValueAt(restoreValue, row, column);
                            }
                        }
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Некорректное число", "Ошибка формата", JOptionPane.ERROR_MESSAGE);
                    
                    // Восстанавливаем старое значение
                    RecIntegral record = recordsList.get(row);
                    double restoreValue = column == 0 ? record.getStep() : (column == 1 ? record.getUpperLimit() : record.getLowerLimit());
                    model.setValueAt(restoreValue, row, column);
                } finally {
                    isUpdating = false;
                }
            }
        }
    });
}

//методы для файлов
private void showError(String message, Exception e) {
    logger.severe(message + ": " + e.getMessage());
    JOptionPane.showMessageDialog(this, message + "\n\n" + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
}

private void updateTableFromList() {
    DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
    model.setRowCount(0); // Очищаем таблицу
    for (RecIntegral record : recordsList) {
        model.addRow(record.toTableRow());
    }
}

private File chooseFile(boolean forSave, String description, String... extensions) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle(forSave ? "Сохранить файл" : "Открыть файл");
    
    // Добавляем фильтры файлов
    for (String ext : extensions) {
        fileChooser.addChoosableFileFilter( new javax.swing.filechooser.FileNameExtensionFilter(ext.toUpperCase() + " файлы (*." + ext + ")", ext));
    }
    
    int result = forSave ? fileChooser.showSaveDialog(this) : fileChooser.showOpenDialog(this);
    
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        
        // Добавляем расширение, если его нет
        if (forSave && selectedFile.getName().indexOf('.') == -1) {
            String ext = extensions[0];
            selectedFile = new File(selectedFile.getAbsolutePath() + "." + ext);
        }
        
        return selectedFile;
    }
    
    return null;
}
//для многопоточности
public void updateResult(double result, int rowIndex, long elapsedTime) {
    if (rowIndex >= 0 && rowIndex < TableModel.getRowCount()) {
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        if (rowIndex < recordsList.size()) {
            RecIntegral record = recordsList.get(rowIndex);
            record.setResult(result);
        }
        model.setValueAt(String.format("%.5f", result), rowIndex, 3);
        
        //обновляем время
        double elapsedMs = elapsedTime / 1_000_000.0;
        TimeValueLabel.setText(String.format("%.3f ms", elapsedMs));
    }
} 

    //Запускает указанное количество TCP клиентов
    private void startTCPClients(int count) {
    // Проверка на максимальное количество клиентов
     if (count > MAX_CLIENTS) {
        JOptionPane.showMessageDialog(this, "Maximum TCP clients: " + MAX_CLIENTS + 
                                      "\nPlease specify a smaller number.", 
                                      "Limit exceeded", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String mainClass = "com.mycompany.lab6.ClientTCP";

        for (int i = 0; i < count; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder(javaBin, "-cp", classpath, mainClass, "localhost", "8080");
            File logFile = new File("client_tcp_" + i + ".log");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
            Process process = processBuilder.start();
            clientProcesses.add(process);
            System.out.println("Started TCP Client #" + i + ", PID: " + process.pid());
            Thread.sleep(Constants.CLIENT_START_DELAY);
        }
        tcpClientsStarted = true;
        tcpClientsCount = count;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error starting TCP clients: " + e.getMessage(), 
                                     "Error", JOptionPane.ERROR_MESSAGE);
        tcpClientsStarted = false;
        tcpClientsCount = 0;
    }
    }

    //Запускает указанное количество UDP клиентов
    private void startUDPClients(int count) {
    // Проверка на максимальное количество клиентов
    if (count > MAX_CLIENTS) {
        JOptionPane.showMessageDialog(this, "Maximum UDP clients: " + MAX_CLIENTS + "\nPlease specify a smaller number.", "Limit exceeded", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        String classpath = System.getProperty("java.class.path");
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String mainClass = "com.mycompany.lab6.ClientUDP";

        for (int i = 0; i < count; i++) {
            ProcessBuilder processBuilder = new ProcessBuilder(javaBin, "-cp", classpath, mainClass, "localhost", "8080");
            File logFile = new File("client_udp_" + i + ".log");
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
            Process process = processBuilder.start();
            clientProcesses.add(process);
            System.out.println("Started UDP Client #" + i + ", PID: " + process.pid());
            Thread.sleep(Constants.CLIENT_START_DELAY);
        }
        udpClientsStarted = true;
        udpClientsCount = count;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error starting UDP clients: " + e.getMessage(), 
                                     "Error", JOptionPane.ERROR_MESSAGE);
        udpClientsStarted = false;
        udpClientsCount = 0;
    }
}

    //Останавливает всех запущенных клиентов
    private void stopClients() { 
      // Останавливаем все процессы клиентов
    for (Process process : clientProcesses) {
        if (process.isAlive()) {
            process.destroy();
            System.out.println("Stopped process with PID: " + process.pid());
        }
    }
    clientProcesses.clear();
    // Останавливаем серверы
    if (tcpServer != null) {
        tcpServer.stop();
        tcpServer = null;
    }
    if (udpServer != null) {
        udpServer.stop();
        udpServer = null;
    }
    tcpClientsStarted = false;
    udpClientsStarted = false;
    tcpClientsCount = 0;
    udpClientsCount = 0;
    // Ждём освобождения порта
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {}
    //JOptionPane.showMessageDialog(this, "All clients and servers stopped", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void checkClientsStatus() {
      int alive = 0;
    for (Process process : clientProcesses) {
        if (process.isAlive()) alive++;
    }
    String message = String.format(
        "=== CLIENTS STATUS ===\n\n" +
        "TCP clients:\n" +
        "  Started: %d\n" +
        "  Status: %s\n\n" +
        "UDP clients:\n" +
        "  Started: %d\n" +
        "  Status: %s\n\n" +
        "Active processes: %d\n" +
        "Max clients limit: %d",
        tcpClientsCount, tcpClientsStarted ? "Active" : "Not started",
        udpClientsCount, udpClientsStarted ? "Active" : "Not started",alive, MAX_CLIENTS);
    JOptionPane.showMessageDialog(this, message, "Client Status", JOptionPane.INFORMATION_MESSAGE);
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
        FilePane = new java.awt.Panel();
        SaveFileBin = new java.awt.Button();
        SaveFileText = new java.awt.Button();
        DownloadFileBin = new java.awt.Button();
        DownloadFileText = new java.awt.Button();
        SaveFileJson = new javax.swing.JButton();
        DownloadFileJson = new javax.swing.JButton();
        FilePaneLabel = new javax.swing.JLabel();
        ThreadingLabel = new javax.swing.JLabel();
        TimeLabel = new javax.swing.JLabel();
        TimeValueLabel = new javax.swing.JLabel();
        DisturbutedCountLabel = new javax.swing.JLabel();
        ClientsLabel = new javax.swing.JLabel();
        CountTCPClientButton = new javax.swing.JButton();
        CountUDPButton = new javax.swing.JButton();
        ClientsTextField = new javax.swing.JTextField();
        StopClientsButton = new javax.swing.JButton();
        CheckClientsStatusButton = new javax.swing.JButton();

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

        FilePane.setBackground(new java.awt.Color(204, 204, 204));
        FilePane.setFont(new java.awt.Font("Dialog", 1, 16)); // NOI18N

        SaveFileBin.setActionCommand("SaveFileBin");
        SaveFileBin.setBackground(new java.awt.Color(255, 255, 255));
        SaveFileBin.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        SaveFileBin.setLabel("Save(Bin)");
        SaveFileBin.addActionListener(this::SaveFileBinActionPerformed);

        SaveFileText.setActionCommand("SaveFileText");
        SaveFileText.setBackground(new java.awt.Color(255, 255, 255));
        SaveFileText.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        SaveFileText.setLabel("Save(Text)");
        SaveFileText.addActionListener(this::SaveFileTextActionPerformed);

        DownloadFileBin.setActionCommand("DownloadFileBin");
        DownloadFileBin.setBackground(new java.awt.Color(255, 255, 255));
        DownloadFileBin.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        DownloadFileBin.setLabel("Download(Bin)");
        DownloadFileBin.addActionListener(this::DownloadFileBinActionPerformed);

        DownloadFileText.setActionCommand("DownloadFileText");
        DownloadFileText.setBackground(new java.awt.Color(255, 255, 255));
        DownloadFileText.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        DownloadFileText.setLabel("Download(Text)");
        DownloadFileText.addActionListener(this::DownloadFileTextActionPerformed);

        SaveFileJson.setBackground(new java.awt.Color(255, 255, 255));
        SaveFileJson.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SaveFileJson.setForeground(new java.awt.Color(0, 0, 0));
        SaveFileJson.setText("Save(Json)");
        SaveFileJson.addActionListener(this::SaveFileJsonActionPerformed);

        DownloadFileJson.setBackground(new java.awt.Color(255, 255, 255));
        DownloadFileJson.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        DownloadFileJson.setForeground(new java.awt.Color(0, 0, 0));
        DownloadFileJson.setText("Download(Json)");
        DownloadFileJson.setActionCommand("DownloadFileJson");
        DownloadFileJson.addActionListener(this::DownloadFileJsonActionPerformed);

        javax.swing.GroupLayout FilePaneLayout = new javax.swing.GroupLayout(FilePane);
        FilePane.setLayout(FilePaneLayout);
        FilePaneLayout.setHorizontalGroup(
            FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DownloadFileBin, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SaveFileBin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(FilePaneLayout.createSequentialGroup()
                        .addComponent(SaveFileText, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SaveFileJson, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(FilePaneLayout.createSequentialGroup()
                        .addComponent(DownloadFileText, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DownloadFileJson, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        FilePaneLayout.setVerticalGroup(
            FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FilePaneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SaveFileBin, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                    .addComponent(SaveFileText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SaveFileJson, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(FilePaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DownloadFileBin, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE)
                    .addComponent(DownloadFileText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DownloadFileJson, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(39, 39, 39))
        );

        SaveFileBin.getAccessibleContext().setAccessibleName("SaveFileBin");
        SaveFileText.getAccessibleContext().setAccessibleName("SaveFileText");
        DownloadFileBin.getAccessibleContext().setAccessibleName("DownloadFileBin");
        DownloadFileText.getAccessibleContext().setAccessibleName("DownloadFileText");
        SaveFileJson.getAccessibleContext().setAccessibleName("SaveFileJson");

        FilePaneLabel.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        FilePaneLabel.setText("Save file:");

        ThreadingLabel.setFont(new java.awt.Font("Segoe UI", 3, 12)); // NOI18N
        ThreadingLabel.setText("with multithreading!");

        TimeLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TimeLabel.setText("Count time:");

        TimeValueLabel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TimeValueLabel.setText("0.000 ms");

        DisturbutedCountLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        DisturbutedCountLabel.setText("Distributed counts (With Client/Server)");
        DisturbutedCountLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        ClientsLabel.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        ClientsLabel.setText("Clients:");

        CountTCPClientButton.setBackground(new java.awt.Color(255, 255, 255));
        CountTCPClientButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CountTCPClientButton.setForeground(new java.awt.Color(0, 0, 0));
        CountTCPClientButton.setText("Count(TCP)");
        CountTCPClientButton.addActionListener(this::CountTCPClientButtonActionPerformed);

        CountUDPButton.setBackground(new java.awt.Color(255, 255, 255));
        CountUDPButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CountUDPButton.setForeground(new java.awt.Color(0, 0, 0));
        CountUDPButton.setText("Count(UDP)");
        CountUDPButton.addActionListener(this::CountUDPButtonActionPerformed);

        ClientsTextField.addActionListener(this::ClientsTextFieldActionPerformed);

        StopClientsButton.setBackground(new java.awt.Color(255, 255, 255));
        StopClientsButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        StopClientsButton.setForeground(new java.awt.Color(0, 0, 0));
        StopClientsButton.setText("Stop Clients");
        StopClientsButton.addActionListener(this::StopClientsButtonActionPerformed);

        CheckClientsStatusButton.setBackground(new java.awt.Color(255, 255, 255));
        CheckClientsStatusButton.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        CheckClientsStatusButton.setForeground(new java.awt.Color(0, 0, 0));
        CheckClientsStatusButton.setText("Clients Status");
        CheckClientsStatusButton.addActionListener(this::CheckClientsStatusButtonActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1))
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
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(ThreadingLabel))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addComponent(CountTCPClientButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                                        .addComponent(CountUDPButton, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(CountButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(ExitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(ClearStringButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(ClearTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(FilePaneLabel)
                                            .addComponent(DisturbutedCountLabel)
                                            .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(ClientsLabel)
                                                .addGap(18, 18, 18)
                                                .addComponent(ClientsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGap(0, 4, Short.MAX_VALUE)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(AddToTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                    .addComponent(FillTableButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(StopClientsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(CheckClientsStatusButton, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE))
                                .addGap(28, 28, 28))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(FilePane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(TimeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TimeValueLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 567, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(TimeLabel)
                            .addComponent(TimeValueLabel)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ThreadingLabel)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(StepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(StepLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(DownLimitLabel)
                            .addComponent(LowerLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(UpLimitLabel)
                            .addComponent(UpperLimitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CountButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ClearStringButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AddToTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(FilePaneLabel)
                        .addGap(1, 1, 1)
                        .addComponent(FilePane, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ExitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(FillTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ClearTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(DisturbutedCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ClientsLabel)
                    .addComponent(ClientsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CountUDPButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(StopClientsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CountTCPClientButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(CheckClientsStatusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        FilePaneLabel.getAccessibleContext().setAccessibleName("FilePaneLabel");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
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

 
             // Запуск вычисления в отдельном потоке
        IntegralThreading calculator = new IntegralThreading(step, lowlim, uplim, selectedRow, this);
        new Thread(calculator).start();
        
        // показываем сообщение о начале вычисления
        model.setValueAt("Вычисляется...", selectedRow, 3);
//            

//         //ЗАМЕР ВРЕМЕНИ НАЧАЛО
//        long startTime = System.nanoTime();
//        double result = RecIntegral.computeIntegral(step, lowlim, uplim);
//        
//            // обновляем результат в коллекции
//               if (selectedRow < recordsList.size()) {
//                   RecIntegral record = recordsList.get(selectedRow);
//                   record.setResult(result);
//               }
//               
//            // Форматируем результат до 5 знаков
//            model.setValueAt(String.format("%.5f", result), selectedRow, 3);
//        // ЗАМЕР ВРЕМЕНИ КОНЕЦ
//        long endTime = System.nanoTime();
//        double elapsedTime = (endTime - startTime) / 1_000_000.0; // конвертация в миллисекунды
//        
//        // Обновление label с временем
//        TimeValueLabel.setText(String.format("%.3f мс", elapsedTime));
//        
        
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
        // получаем модель и удаляем строку из таблицы и коллекции
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        model.removeRow(selectedRow);
        recordsList.remove(selectedRow);
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
        
        double step, lowlim, uplim;
        try{
             step = Double.parseDouble(stepText);
             lowlim = Double.parseDouble(lowlimText);
             uplim = Double.parseDouble(uplimText); 
         }
         catch(NumberFormatException e)
         {JOptionPane.showMessageDialog(this, "Ошибка парсинга переменных.", "Ошибка", JOptionPane.ERROR_MESSAGE);
               return;
           }
           
            // Проверка диапазона значений (0.000001 до 1000000)
           if (step < RecIntegral.MIN_VALUE || step > RecIntegral.MAX_VALUE || lowlim < RecIntegral.MIN_VALUE || lowlim > RecIntegral.MAX_VALUE || uplim < RecIntegral.MIN_VALUE || uplim > RecIntegral.MAX_VALUE) {
               String message = String.format( "Значения должны быть в диапазоне от %.6f до %.6f\n\n" + "Введенные значения:\n" + "Шаг: %.6f\n" + "Нижний предел: %.6f\n" + "Верхний предел: %.6f",
                   RecIntegral.MIN_VALUE, RecIntegral.MAX_VALUE, step, lowlim, uplim);
               
               JOptionPane.showMessageDialog(this, message, "Ошибка валидации", JOptionPane.WARNING_MESSAGE);
               return;
           }
           if (lowlim >= uplim) {
                JOptionPane.showMessageDialog(this, "Нижний предел должен быть меньше верхнего предела", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (step <= 0) {JOptionPane.showMessageDialog(this, "Шаг должен быть положительным числом", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (step > uplim - lowlim) {JOptionPane.showMessageDialog(this, "Шаг должен быть меньше или равен разнице верхнего и нижнего пределов", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
           
             if (tcpClientsStarted || udpClientsStarted) {
                 JOptionPane.showMessageDialog(this, "Нельзя добавлять записи во время работы клиентов.\n" + "Остановите клиентов перед добавлением новых данных.", "Ошибка",  JOptionPane.WARNING_MESSAGE);
                 return;
             }
            
           // Создаем объект (конструктор без исключений)
           RecIntegral record = new RecIntegral(step, uplim, lowlim);

           // добавляем в список
           recordsList.add(record);
           // добавление в таблицу
           DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
           model.addRow(record.toTableRow());

           // Очищаем поля
           StepTextField.setText("");
           LowerLimitTextField.setText("");
           UpperLimitTextField.setText("");

       } catch (NumberFormatException e) {
           JOptionPane.showMessageDialog(this, "Введите корректные числовые значения (например: 0.1, 0.5, 1.0)", "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
       }
    }//GEN-LAST:event_AddToTableButtonActionPerformed

    private void StepTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StepTextFieldActionPerformed

    }//GEN-LAST:event_StepTextFieldActionPerformed

    private void UpperLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_UpperLimitTextFieldActionPerformed

    }//GEN-LAST:event_UpperLimitTextFieldActionPerformed

    private void LowerLimitTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LowerLimitTextFieldActionPerformed

    }//GEN-LAST:event_LowerLimitTextFieldActionPerformed

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitButtonActionPerformed
    stopClients();
    // Останавливаем серверы
    if (tcpServer != null) { tcpServer.stop();}
    if (udpServer != null) {udpServer.stop();}
    System.exit(0);
    }//GEN-LAST:event_ExitButtonActionPerformed

    private void ClearTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearTableButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
        model.setRowCount(0);
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

    private void SaveFileBinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFileBinActionPerformed
         if (recordsList.isEmpty()) {JOptionPane.showMessageDialog(this, "Нет данных для сохранения", "Информация", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    File file = chooseFile(true, "Выберите файл для сохранения", "bin", "ser");
    if (file == null) return;
    
    try {
        FileManager.saveToBinaryFile(recordsList, file);
        JOptionPane.showMessageDialog(this, "Данные успешно сохранены в бинарный файл:\n" + file.getName(), "Успех", 
            JOptionPane.INFORMATION_MESSAGE);
    } catch (IntegralException e) {
        showError("Ошибка валидации данных при сохранении", e);
    } catch (IOException e) {
        showError("Ошибка при сохранении бинарного файла", e);
    }
    }//GEN-LAST:event_SaveFileBinActionPerformed

    private void DownloadFileBinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadFileBinActionPerformed
        File file = chooseFile(false, "Выберите бинарный файл для загрузки", "bin", "ser");
    if (file == null) return;
    
    try {
        LinkedList<RecIntegral> loadedRecords = FileManager.loadFromBinaryFile(file);
        
        // Спрашиваем пользователя, что делать с загруженными данными
        int choice = JOptionPane.showConfirmDialog(this,"Загруженные данные будут добавлены к текущим.\nПродолжить?","Подтверждение",JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            recordsList.addAll(loadedRecords);
            updateTableFromList();
            JOptionPane.showMessageDialog(this, String.format("Загружено %d записей из файла:\n%s", loadedRecords.size(), file.getName()), "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (ClassNotFoundException e) {
        showError("Неверный формат файла или версия класса", e);
    } catch (IntegralException e) {
        showError("Ошибка валидации загруженных данных", e);
    } catch (IOException e) {
        showError("Ошибка при загрузке бинарного файла", e);
    }
    }//GEN-LAST:event_DownloadFileBinActionPerformed

    private void SaveFileTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFileTextActionPerformed
         if (recordsList.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Нет данных для сохранения", "Информация", JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    File file = chooseFile(true, "Выберите файл для сохранения", "txt", "csv");
    if (file == null) return;
    
    try {
        FileManager.saveToTextFile(recordsList, file);
        JOptionPane.showMessageDialog(this, "Данные успешно сохранены в текстовый файл:\n" + file.getName(), "Успех", JOptionPane.INFORMATION_MESSAGE);
    } catch (IntegralException e) {
        showError("Ошибка валидации данных при сохранении", e);
    } catch (IOException e) {
        showError("Ошибка при сохранении текстового файла", e);
    }
    }//GEN-LAST:event_SaveFileTextActionPerformed

    private void DownloadFileTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadFileTextActionPerformed
        File file = chooseFile(false, "Выберите текстовый файл для загрузки", "txt", "csv");
    if (file == null) return;
    
    try {
        LinkedList<RecIntegral> loadedRecords = FileManager.loadFromTextFile(file);
        
        int choice = JOptionPane.showConfirmDialog(this, "Загруженные данные будут добавлены к текущим.\nПродолжить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            recordsList.addAll(loadedRecords);
            updateTableFromList();
            JOptionPane.showMessageDialog(this, String.format("Загружено %d записей из файла:\n%s", loadedRecords.size(), file.getName()), "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (IntegralException e) {
        showError("Ошибка валидации загруженных данных", e);
    } catch (IOException e) {
        showError("Ошибка при загрузке текстового файла", e);
    } catch (Exception e) {
        showError("Неожиданная ошибка при загрузке файла", e);
    }
    }//GEN-LAST:event_DownloadFileTextActionPerformed

    private void SaveFileJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFileJsonActionPerformed
         if (recordsList.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Нет данных для сохранения", "Информация", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    File file = chooseFile(true, "Выберите файл для сохранения", "json");
    if (file == null) return;

    try {
        FileManager.saveToJsonFile(recordsList, file);
        JOptionPane.showMessageDialog(this, "Данные успешно сохранены в JSON файл:\n" + file.getName(), "Успех", JOptionPane.INFORMATION_MESSAGE);
    } catch (IntegralException e) {
        showError("Ошибка валидации данных при сохранении", e);
    } catch (IOException e) {
        showError("Ошибка при сохранении JSON файла", e);
    }
    }//GEN-LAST:event_SaveFileJsonActionPerformed

    private void DownloadFileJsonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DownloadFileJsonActionPerformed
        File file = chooseFile(false, "Выберите JSON файл для загрузки", "json");
    if (file == null) return;

    try {
        LinkedList<RecIntegral> loadedRecords = FileManager.loadFromJsonFile(file);
        
        int choice = JOptionPane.showConfirmDialog(this, "Загруженные данные будут добавлены к текущим.\nПродолжить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            recordsList.addAll(loadedRecords);
            updateTableFromList();
            JOptionPane.showMessageDialog(this, String.format("Загружено %d записей из файла:\n%s", loadedRecords.size(), file.getName()), "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (IntegralException e) {
        showError("Ошибка валидации загруженных данных", e);
    } catch (IOException e) {
        showError("Ошибка при загрузке JSON файла", e);
    } catch (Exception e) {
        showError("Неожиданная ошибка при загрузке файла", e);
    }
    }//GEN-LAST:event_DownloadFileJsonActionPerformed

    private void CountTCPClientButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CountTCPClientButtonActionPerformed
     if (computingInProgress) {
        JOptionPane.showMessageDialog(this, "Computation already in progress. Please wait.", "Info", JOptionPane.WARNING_MESSAGE);
        return;
    }
    int selectedRow = TableModel.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select a row to compute", "Info", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        double step = (Double) TableModel.getValueAt(selectedRow, 0);
        double uplim = (Double) TableModel.getValueAt(selectedRow, 1);
        double lowlim = (Double) TableModel.getValueAt(selectedRow, 2);
        int clientCount = Integer.parseInt(ClientsTextField.getText());
        if (clientCount < 1) {
            JOptionPane.showMessageDialog(this, "Number of clients must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        computingInProgress = true;
        TimeValueLabel.setText("TCP distributed computation in progress...");

        // Останавливаем всё
        stopClients();

        // Запускаем сервер
        tcpServer = new ServerTCP(8080, this);
        tcpServerThread = new Thread(() -> tcpServer.start());
        tcpServerThread.start();
        Thread.sleep(1000); // даём серверу время на запуск
        // Проверяем, запустился ли сервер
        if (tcpServer == null || !tcpServer.isStarted()) {
            JOptionPane.showMessageDialog(this, "TCP server failed to start.", "Error", JOptionPane.ERROR_MESSAGE);
            computingInProgress = false;
            return;
        }
        // Запускаем клиентов
        startTCPClients(clientCount);

        // Ждём подключений
        long startWait = System.currentTimeMillis();
        while (tcpServer.getClientsCount() < clientCount && System.currentTimeMillis() - startWait < 5000) {
            Thread.sleep(100);
        }
        if (tcpServer.getClientsCount() < clientCount) {
            JOptionPane.showMessageDialog(this, "Not all clients connected within timeout", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        // Отправляем задание
        IntegralTask task = new IntegralTask(step, lowlim, uplim, 0, clientCount);
        tcpServer.sendTaskToAll(task, clientCount);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        computingInProgress = false;
    }
    }//GEN-LAST:event_CountTCPClientButtonActionPerformed

    private void CountUDPButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CountUDPButtonActionPerformed
      if (computingInProgress) {
        JOptionPane.showMessageDialog(this, "Computation already in progress. Please wait.", "Info", JOptionPane.WARNING_MESSAGE);
        return;
    }
    int selectedRow = TableModel.getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Select a row to compute", "Info", JOptionPane.WARNING_MESSAGE);
        return;
    }
    try {
        double step = (Double) TableModel.getValueAt(selectedRow, 0);
        double uplim = (Double) TableModel.getValueAt(selectedRow, 1);
        double lowlim = (Double) TableModel.getValueAt(selectedRow, 2);
        int clientCount = Integer.parseInt(ClientsTextField.getText());
        if (clientCount < 1) {
            JOptionPane.showMessageDialog(this, "Number of clients must be > 0", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        computingInProgress = true;
        TimeValueLabel.setText("UDP distributed computation in progress...");

        stopClients();

        udpServer = new ServerUDP(8080, this);
        udpServerThread = new Thread(() -> udpServer.start());
        udpServerThread.start();
        Thread.sleep(1000);
        if (udpServer == null || !udpServer.isStarted()) {
            JOptionPane.showMessageDialog(this, "UDP server failed to start.", "Error", JOptionPane.ERROR_MESSAGE);
            computingInProgress = false;
            return;
        }
        startUDPClients(clientCount);

        long startWait = System.currentTimeMillis();
        while (udpServer.getClientsCount() < clientCount && System.currentTimeMillis() - startWait < 5000) {
            Thread.sleep(100);
        }
        if (udpServer.getClientsCount() < clientCount) {
            JOptionPane.showMessageDialog(this, "Not all UDP clients connected within timeout", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        IntegralTask task = new IntegralTask(step, lowlim, uplim, 0, clientCount);
        udpServer.sendTaskToAll(task, clientCount);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        computingInProgress = false;
    }
    }//GEN-LAST:event_CountUDPButtonActionPerformed

    
    private void ClientsTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClientsTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ClientsTextFieldActionPerformed

    private void StopClientsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StopClientsButtonActionPerformed
        stopClients();
    }//GEN-LAST:event_StopClientsButtonActionPerformed

    private void CheckClientsStatusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CheckClientsStatusButtonActionPerformed
        checkClientsStatus();
    }//GEN-LAST:event_CheckClientsStatusButtonActionPerformed
    
    public void updateDistributedResult(double totalResult, long maxComputationTime, long totalTime, int successful, int failed) {SwingUtilities.invokeLater(() -> {      
         // Обновляем таблицу с результатом
         computingInProgress = false; // разрешаем новый запуск
            int selectedRow = TableModel.getSelectedRow();
            if (selectedRow != -1) {
                DefaultTableModel model = (DefaultTableModel) TableModel.getModel();
                if (selectedRow < recordsList.size()) {
                    RecIntegral record = recordsList.get(selectedRow);
                    record.setResult(totalResult);
                }
                model.setValueAt(String.format("%.5f", totalResult), selectedRow, 3);
            }
            TimeValueLabel.setText(String.format("%.3f ms (distributed)", totalTime / 1_000_000.0));
            String message = String.format(
                "=== Distributed computation result ===\n\n" +
                "Integral result: %.5f\n" +
                "Successful clients: %d\n" +
                "Failed clients: %d\n" +
                "Max computation time: %.3f ms\n" +
                "Total execution time: %.3f ms\n\n" +
                "Average time per client: %.3f ms",
                totalResult, successful, failed,
                maxComputationTime / 1_000_000.0,
                totalTime / 1_000_000.0,
                (totalTime / 1_000_000.0) / (successful + failed));
            JOptionPane.showMessageDialog(this, message, "Distributed computation result", JOptionPane.INFORMATION_MESSAGE);
        });
}
    
    
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
         java.awt.EventQueue.invokeLater(() -> new Lab3JFrame().setVisible(true));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddToTableButton;
    private javax.swing.JButton CheckClientsStatusButton;
    private javax.swing.JButton ClearStringButton;
    private javax.swing.JButton ClearTableButton;
    private javax.swing.JLabel ClientsLabel;
    private javax.swing.JTextField ClientsTextField;
    private javax.swing.JButton CountButton;
    private javax.swing.JButton CountTCPClientButton;
    private javax.swing.JButton CountUDPButton;
    private javax.swing.JLabel DisturbutedCountLabel;
    private javax.swing.JLabel DownLimitLabel;
    private java.awt.Button DownloadFileBin;
    private javax.swing.JButton DownloadFileJson;
    private java.awt.Button DownloadFileText;
    private javax.swing.JButton ExitButton;
    private java.awt.Panel FilePane;
    private javax.swing.JLabel FilePaneLabel;
    private javax.swing.JButton FillTableButton;
    private javax.swing.JTextField LowerLimitTextField;
    private java.awt.Button SaveFileBin;
    private javax.swing.JButton SaveFileJson;
    private java.awt.Button SaveFileText;
    private javax.swing.JLabel StepLabel;
    private javax.swing.JTextField StepTextField;
    private javax.swing.JButton StopClientsButton;
    private javax.swing.JTable TableModel;
    private javax.swing.JLabel ThreadingLabel;
    private javax.swing.JLabel TimeLabel;
    private javax.swing.JLabel TimeValueLabel;
    private javax.swing.JLabel UpLimitLabel;
    private javax.swing.JTextField UpperLimitTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
