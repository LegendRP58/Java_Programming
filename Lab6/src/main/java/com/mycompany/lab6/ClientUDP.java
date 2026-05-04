package com.mycompany.lab6;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientUDP {
    private InetAddress serverAddress;
    private int serverPort;
    
    public ClientUDP(String serverHost, int serverPort) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(serverHost);
        this.serverPort = serverPort;
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(0);

            byte[] registerData = new byte[1];
            DatagramPacket registerPacket = new DatagramPacket(registerData, registerData.length, serverAddress, serverPort);
            socket.send(registerPacket);
            System.out.println("UDP client registered");

            while (true) {
                byte[] receiveBuffer = new byte[Constants.UDP_BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
                ObjectInputStream ois = new ObjectInputStream(bais);
                IntegralTask task = (IntegralTask) ois.readObject();

                System.out.println("UDP client received task: " + task + 
                                 " (segment: " + task.getLowerLimit() + " to " + task.getUpperLimit() + ")");

                long startTime = System.nanoTime();
                double result = computeIntegralMultithreaded(
                        task.getStep(),
                        task.getLowerLimit(),
                        task.getUpperLimit()
                );
                long computationTime = System.nanoTime() - startTime;

                IntegralResult integralResult = new IntegralResult(result, task.getClientId(), computationTime);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(integralResult);
                oos.flush();

                byte[] sendData = baos.toByteArray();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
                socket.send(sendPacket);

                System.out.println("UDP client " + task.getClientId() + " finished computation in " +
                        computationTime / 1_000_000.0 + " ms");
            }

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
        List<double[]> subIntervals = new ArrayList<>();
        double x = startLimit;
        while (x < endLimit) {
            double nextX = Math.min(x + step, endLimit);
            subIntervals.add(new double[]{x, nextX});
            x = nextX;
        }

        int total = subIntervals.size();
        int numThreads = Constants.NUM_CLIENT_THREADS;
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
        try {
            String host = args.length > 0 ? args[0] : "localhost";
            int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;

            ClientUDP client = new ClientUDP(host, port);
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}