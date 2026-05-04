package com.mycompany.lab6;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class IntegralThreading implements Runnable {
    private final double step;
    private final double lowerLimit;
    private final double upperLimit;
    private final int rowIndex;
    private final Lab3JFrame frame;
    private double result;
    private long elapsedTime;
    
    public IntegralThreading(double step, double lowerLimit, double upperLimit, int rowIndex, Lab3JFrame frame) {
        this.step = step;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.rowIndex = rowIndex;
        this.frame = frame;
    }
    
    @Override
    public void run() {
        long startTime = System.nanoTime();
        
        // Строим глобальную сетку элементарных шагов (аналогично клиентам)
        List<double[]> subIntervals = new ArrayList<>();
        double x = lowerLimit;
        while (x < upperLimit) {
            double nextX = Math.min(x + step, upperLimit);
            subIntervals.add(new double[]{x, nextX});
            x = nextX;
        }
        
        int totalIntervals = subIntervals.size();
        int numThreads = Constants.NUM_CLIENT_THREADS;
        int threadsToUse = Math.min(numThreads, totalIntervals);
        Thread[] threads = new Thread[threadsToUse];
        double[] partialSums = new double[threadsToUse];
        
        int perThread = totalIntervals / threadsToUse;
        int remainder = totalIntervals % threadsToUse;
        int startIdx = 0;
        
        for (int t = 0; t < threadsToUse; t++) {
            final int threadIdx = t;
            final int count = perThread + (t < remainder ? 1 : 0);
            final int begin = startIdx;
            final int end = startIdx + count;
            
            threads[t] = new Thread(() -> {
                double sum = 0.0;
                for (int i = begin; i < end; i++) {
                    double[] iv = subIntervals.get(i);
                    double x1 = iv[0];
                    double x2 = iv[1];
                    double h = x2 - x1;
                    double y1 = RecIntegral.func(x1);
                    double y2 = RecIntegral.func(x2);
                    sum += (y1 + y2) * h / 2.0;
                }
                partialSums[threadIdx] = sum;
            });
            threads[t].start();
            startIdx += count;
        }
        
        for (int t = 0; t < threadsToUse; t++) {
            try {
                threads[t].join();
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(null, "Возникло прерывание! Ошибка!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        double sum = 0.0;
        for (double s : partialSums) {
            sum += s;
        }
        this.result = sum;
        
        long endTime = System.nanoTime();
        this.elapsedTime = endTime - startTime;
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            frame.updateResult(result, rowIndex, elapsedTime);
        });
    }
}