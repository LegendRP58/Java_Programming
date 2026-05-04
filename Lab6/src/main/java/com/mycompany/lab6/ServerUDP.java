package com.mycompany.lab6;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServerUDP {
    private DatagramSocket socket;
    private int port;
    private Lab3JFrame gui;
    private List<InetSocketAddress> clients = Collections.synchronizedList(new ArrayList<>());
    private List<IntegralResult> results = Collections.synchronizedList(new ArrayList<>());
    private int expectedClients;
    private long startTime;
    private boolean running = true;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private boolean computing = false;
    private boolean started = false;

    public int getClientsCount() { 
        return clients.size(); 
    }
    
    public boolean isStarted() { 
        return started; 
    }

    public ServerUDP(int port, Lab3JFrame gui) {
        this.port = port;
        this.gui = gui;
    }

    public void start() {
        int retries = 3;
        while (retries > 0) {
            try {
                socket = new DatagramSocket(port);
                started = true;
                System.out.println("UDP server started on port " + port);
                executor.submit(() -> {
                    while (running) {
                        byte[] buffer = new byte[Constants.UDP_BUFFER_SIZE];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        try {
                            socket.receive(packet);
                            handlePacket(packet);
                        } catch (IOException e) {
                            if (running) e.printStackTrace();
                        }
                    }
                });
                break;
            } catch (BindException e) {
                retries--;
                if (retries > 0) {
                    System.err.println("UDP port " + port + " is busy, retrying in 1 second...");
                    try { Thread.sleep(1000); } catch (InterruptedException ie) {}
                } else {
                    e.printStackTrace();
                }
            } catch (SocketException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void stop() {
        running = false;
        executor.shutdown();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        started = false;
    }

    private void handlePacket(DatagramPacket packet) {
        byte[] data = packet.getData();
        int length = packet.getLength();

        if (length <= 1) {
            InetSocketAddress clientAddress = new InetSocketAddress(packet.getAddress(), packet.getPort());
            if (!clients.contains(clientAddress)) {
                clients.add(clientAddress);
                System.out.println("Registered UDP client: " + clientAddress + " (total: " + clients.size() + ")");
            }
            return;
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(data, 0, length);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();

            if (obj instanceof IntegralResult) {
                IntegralResult result = (IntegralResult) obj;
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
                        System.out.println("Received UDP result from client " + result.getClientId() +
                                 " (time: " + result.getComputationTimeMs() + " ms)");

                        if (results.size() == expectedClients && expectedClients > 0) {
                            aggregateResults();
                        }
                    }
                }
            } else {
                System.err.println("Unexpected object type: " + obj.getClass());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            System.err.println("Warning: only " + clientsToUse + " UDP clients available out of " + clientCount);
        }

        // Строим глобальную сетку
        List<Double> points = new ArrayList<>();
        points.add(task.getLowerLimit());
        double x = task.getLowerLimit();
        while (x < task.getUpperLimit()) {
            double nextX = Math.min(x + task.getStep(), task.getUpperLimit());
            points.add(nextX);
            x = nextX;
        }
        int totalSubintervals = points.size() - 1;
        int effectiveClients = Math.min(clientsToUse, totalSubintervals);
        if (effectiveClients < clientsToUse) {
            System.err.println("Reducing UDP client usage to " + effectiveClients + " due to few subintervals.");
        }

        int base = totalSubintervals / effectiveClients;
        int rem = totalSubintervals % effectiveClients;
        int startIdx = 0;

        for (int i = 0; i < effectiveClients; i++) {
            final int clientId = i;
            InetSocketAddress addr = clients.get(i);
            int count = base + (i < rem ? 1 : 0);
            int endIdx = startIdx + count;
            final double segStart = points.get(startIdx);
            final double segEnd = points.get(endIdx);
            startIdx = endIdx;

            executor.submit(() -> {
                try {
                    IntegralTask clientTask = new IntegralTask(
                            task.getStep(),
                            segStart,
                            segEnd,
                            clientId,
                            effectiveClients
                    );
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(clientTask);
                    oos.flush();
                    byte[] data = baos.toByteArray();
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, 
                                                                   addr.getAddress(), addr.getPort());
                    socket.send(sendPacket);
                    System.out.println("Sent UDP task to client " + clientId + " at " + addr + 
                                     " (segment: " + segStart + " to " + segEnd + ")");
                } catch (Exception e) {
                    e.printStackTrace();
                    IntegralResult errorResult = new IntegralResult(clientId, "Send error: " + e.getMessage());
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
                    if (results.size() == expectedClients) {
                        aggregateResults();
                    }
                }
            });
        }
        
        executor.submit(() -> {
            try {
                Thread.sleep(Constants.CLIENT_CONNECTION_TIMEOUT);
                if (results.size() < expectedClients) {
                    System.err.println("Timeout: received only " + results.size() + 
                                     " results out of " + expectedClients);
                    aggregateResults();
                }
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
                System.out.printf("  UDP Client %d: result=%.5f, time=%.3f ms%n",
                        result.getClientId(), result.getPartialSum(), result.getComputationTimeMs());
            } else {
                failed++;
                System.err.println("  UDP Client " + result.getClientId() + " error: " + result.getErrorMessage());
            }
        }

        long totalTime = System.nanoTime() - startTime;

        System.out.printf("=== UDP aggregation ===\n");
        System.out.printf("Successful: %d, failed: %d\n", successful, failed);
        System.out.printf("Total result: %.5f\n", total);
        System.out.printf("Max computation time: %.3f ms\n", maxComputationTime / 1_000_000.0);
        System.out.printf("Total time: %.3f ms\n", totalTime / 1_000_000.0);

        if (gui != null) {
            gui.updateDistributedResult(total, maxComputationTime, totalTime, successful, failed);
        }
    }
}