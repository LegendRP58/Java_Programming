package com.mycompany.lab6;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientTCP {
    private String serverHost;
    private int serverPort;
    
    public ClientTCP(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    public void start() {
        try (Socket socket = new Socket(serverHost, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("TCP client connected to server");

            while (true) {
                IntegralTask task = (IntegralTask) in.readObject();
                System.out.println("TCP client received task: " + task + 
                                 " (segment: " + task.getLowerLimit() + " to " + task.getUpperLimit() + ")");

                long startTime = System.nanoTime();
                double result = computeIntegralMultithreaded(
                        task.getStep(),
                        task.getLowerLimit(),
                        task.getUpperLimit()
                );
                long computationTime = System.nanoTime() - startTime;

                IntegralResult integralResult = new IntegralResult(result, task.getClientId(), computationTime);
                out.writeObject(integralResult);
                out.flush();

                System.out.println("TCP client " + task.getClientId() + " finished computation in " +
                        computationTime / 1_000_000.0 + " ms");
            }

        } catch (EOFException e) {
            System.out.println("Server closed connection, exiting.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Вычисляет интеграл на отрезке [startLimit, endLimit] с шагом step,
     * распределяя элементарные шаги между потоками.
     * Гарантирует, что множество точек вычислений совпадает с глобальной сеткой.
     */
    private double computeIntegralMultithreaded(double step, double startLimit, double endLimit) {
        // 1. Строим список элементарных шагов (глобальная сетка внутри сегмента)
        List<double[]> subIntervals = new ArrayList<>();
        double x = startLimit;
        while (x < endLimit) {
            double nextX = Math.min(x + step, endLimit);
            subIntervals.add(new double[]{x, nextX});
            x = nextX;
        }

        int total = subIntervals.size();
        int numThreads = Constants.NUM_CLIENT_THREADS;
        // Если шагов меньше, чем потоков, ограничим число потоков
        int threadsToUse = Math.min(numThreads, total);
        Thread[] threads = new Thread[threadsToUse];
        double[] partialSums = new double[threadsToUse];

        int perThread = total / threadsToUse;
        int remainder = total % threadsToUse;
        int startIdx = 0;

        for (int t = 0; t < threadsToUse; t++) {
            final int threadIdx = t;
            final int count = perThread + (t < remainder ? 1 : 0);
            final int begin = startIdx;
            final int end = startIdx + count;   // исключительный индекс

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
                // ignore
            }
        }

        double totalSum = 0.0;
        for (double s : partialSums) {
            totalSum += s;
        }
        return totalSum;
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

        ClientTCP client = new ClientTCP(host, port);
        client.start();
    }
}