package com.mycompany.lab5;

import javax.swing.JOptionPane;

public class IntegralThreading implements Runnable {
    private final double step;
    private final double lowerLimit;
    private final double upperLimit;
    private final int rowIndex;
    private final Lab3JFrame frame;
    private double result;
    
    public IntegralThreading (double step, double lowerLimit, double upperLimit, int rowIndex, Lab3JFrame frame) {
        this.step = step;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.rowIndex = rowIndex;
        this.frame = frame;
    }
    
    @Override
    public void run() {
        // Вычисление интеграла в 8 потоках
        int numThreads = 8;
        double segmentLength = (upperLimit - lowerLimit) / numThreads;
        double[] partialResults = new double[numThreads];
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            final int threadIndex = i;
            final double segmentStart = lowerLimit + i * segmentLength;
            final double segmentEnd = (i == numThreads - 1) ? upperLimit : lowerLimit + (i + 1) * segmentLength;
            
            threads[i] = new Thread(() -> {
                double sum = 0.0;
                double x = segmentStart;
                while (x < segmentEnd) {
                    double currentStep = Math.min(step, segmentEnd - x);
                    double nextX = x + currentStep;
                    double y1 = Math.sin(x * x);
                    double y2 = Math.sin(nextX * nextX);
                    sum += (y1 + y2) * (nextX - x) / 2.0;
                    x = nextX;
                }
                partialResults[threadIndex] = sum;
            });
            threads[i].start();
        }
        
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "Возникло прерывание! Ошибка!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        double total = 0.0;
        for (double partial : partialResults) {
            total += partial;
        }
        this.result = total;
        
        // Возврат результата в основной поток
        javax.swing.SwingUtilities.invokeLater(() -> {
            frame.updateResult(result, rowIndex);
        });
    }
}