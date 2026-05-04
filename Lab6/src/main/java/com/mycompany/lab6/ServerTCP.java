package com.mycompany.lab6;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerTCP {
    private int port;
    private Lab3JFrame gui;
    private List<ClientInfo> clients = Collections.synchronizedList(new ArrayList<>());
    private List<IntegralResult> results = Collections.synchronizedList(new ArrayList<>());
    private int expectedClients;
    private long startTime;
    private ServerSocket serverSocket;
    private boolean running = true;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private boolean computing = false;
    private boolean started = false;

    private static class ClientInfo {
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        
        ClientInfo(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
            this.socket = socket;
            this.out = out;
            this.in = in;
        }
    }

    public int getClientsCount() { 
        return clients.size(); 
    }
    
    public boolean isStarted() { 
        return started; 
    }

    public ServerTCP(int port, Lab3JFrame gui) {
        this.port = port;
        this.gui = gui;
    }

    public void start() {
        int retries = 3;
        while (retries > 0) {
            try {
                serverSocket = new ServerSocket(port);
                started = true;
                System.out.println("TCP server started on port " + port);
                executor.submit(() -> {
                    while (running) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                            ClientInfo info = new ClientInfo(clientSocket, out, in);
                            clients.add(info);
                            System.out.println("Connected TCP client: " + clientSocket.getInetAddress() +
                                     " (total: " + clients.size() + ")");
                        } catch (IOException e) {
                            if (running) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                break;
            } catch (BindException e) {
                retries--;
                if (retries > 0) {
                    System.err.println("Port " + port + " is busy, retrying in 1 second...");
                    try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                } else {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void stop() {
        running = false;
        executor.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            List<ClientInfo> clientsCopy = new ArrayList<>(clients);
            for (ClientInfo info : clientsCopy) {
                try {
                    info.in.close();
                    info.out.close();
                    info.socket.close();
                } catch (IOException e) { /* ignore */ }
            }
            clients.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        started = false;
    }

    public void sendTaskToAll(IntegralTask task, int clientCount) {
        if (computing) {
            System.err.println("Another computation is already in progress");
            return;
        }
        computing = true;
        this.expectedClients = clientCount;
        this.results.clear();
        this.startTime = System.nanoTime();

        int clientsToUse = Math.min(clientCount, clients.size());
        if (clientsToUse < clientCount) {
            System.err.println("Warning: only " + clientsToUse + " clients available out of " + clientCount);
        }

        // 1. Строим глобальную сетку границ элементарных шагов
        List<Double> points = new ArrayList<>();
        points.add(task.getLowerLimit());
        double x = task.getLowerLimit();
        while (x < task.getUpperLimit()) {
            double nextX = Math.min(x + task.getStep(), task.getUpperLimit());
            points.add(nextX);
            x = nextX;
        }
        int totalSubintervals = points.size() - 1;

        // 2. Ограничиваем число используемых клиентов количеством подынтервалов
        int effectiveClients = Math.min(clientsToUse, totalSubintervals);
        if (effectiveClients < clientsToUse) {
            System.err.println("Warning: reducing client usage to " + effectiveClients + " because of too few subintervals.");
        }

        // 3. Распределяем подынтервалы между клиентами
        int base = totalSubintervals / effectiveClients;
        int rem = totalSubintervals % effectiveClients;
        int startIdx = 0;

        CountDownLatch latch = new CountDownLatch(effectiveClients);

        for (int i = 0; i < effectiveClients; i++) {
            final int clientId = i;
            ClientInfo info = clients.get(i);
            int count = base + (i < rem ? 1 : 0);
            int endIdx = startIdx + count;
            final double segStart = points.get(startIdx);
            final double segEnd = points.get(endIdx);
            startIdx = endIdx;

            IntegralTask clientTask = new IntegralTask(
                    task.getStep(),
                    segStart,
                    segEnd,
                    clientId,
                    effectiveClients
            );
            clientTask.setTaskCreationTime(task.getTaskCreationTime());

            executor.submit(() -> {
                try {
                    synchronized (info.out) {
                        info.out.writeObject(clientTask);
                        info.out.flush();
                    }
                    IntegralResult result = (IntegralResult) info.in.readObject();
                    synchronized (results) {
                        boolean alreadyExists = false;
                        for (IntegralResult existing : results) {
                            if (existing.getClientId() == result.getClientId()) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (!alreadyExists) {
                            results.add(result);
                        }
                    }
                    System.out.println("Received result from TCP client " + clientId +
                             " (time: " + result.getComputationTimeMs() + " ms)");
                } catch (Exception e) {
                    e.printStackTrace();
                    IntegralResult errorResult = new IntegralResult(clientId, "Error: " + e.getMessage());
                    synchronized (results) {
                        boolean alreadyExists = false;
                        for (IntegralResult existing : results) {
                            if (existing.getClientId() == errorResult.getClientId()) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (!alreadyExists) {
                            results.add(errorResult);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        executor.submit(() -> {
            try {
                latch.await();
                aggregateResults();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                computing = false;
            }
        });
    }

    private void aggregateResults() {
        double total = 0;
        long maxComputationTime = 0;
        int successful = 0;
        int failed = 0;

        List<IntegralResult> resultsCopy;
        synchronized (results) {
            resultsCopy = new ArrayList<>(results);
        }

        for (IntegralResult result : resultsCopy) {
            if (result.isSuccess()) {
                total += result.getPartialSum();
                maxComputationTime = Math.max(maxComputationTime, result.getComputationTime()); 
                successful++;
                System.out.printf("  TCP Client %d: result=%.5f, time=%.3f ms%n",
                        result.getClientId(), result.getPartialSum(), result.getComputationTimeMs());
            } else {
                failed++;
                System.err.println("  TCP Client " + result.getClientId() + " error: " + result.getErrorMessage());
            }
        }

        long totalTime = System.nanoTime() - startTime;

        System.out.printf("=== TCP aggregation ===\n");
        System.out.printf("Successful: %d, failed: %d\n", successful, failed);
        System.out.printf("Total result: %.5f\n", total);
        System.out.printf("Max computation time: %.3f ms\n", maxComputationTime / 1_000_000.0);
        System.out.printf("Total time: %.3f ms\n", totalTime / 1_000_000.0);

        if (gui != null) {
            gui.updateDistributedResult(total, maxComputationTime, totalTime, successful, failed);
        }
    }
}