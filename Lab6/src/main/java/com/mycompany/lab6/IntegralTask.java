package com.mycompany.lab6;

import java.io.Serializable;

public class IntegralTask implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double step;
    private double lowerLimit;
    private double upperLimit;
    private int clientId;
    private int totalClients;
    private long taskCreationTime;
    
    public IntegralTask() {
        this.taskCreationTime = System.nanoTime();
    }
    
    public IntegralTask(double step, double lowerLimit, double upperLimit, 
                        int clientId, int totalClients) {
        this.step = step;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.clientId = clientId;
        this.totalClients = totalClients;
        this.taskCreationTime = System.nanoTime();
    }
    
    // Геттеры
    public double getStep() { return step; }
    public double getLowerLimit() { return lowerLimit; }
    public double getUpperLimit() { return upperLimit; }
    public int getClientId() { return clientId; }
    public int getTotalClients() { return totalClients; }
    public long getTaskCreationTime() { return taskCreationTime; }
    
    
    // Сеттеры
    public void setStep(double step) { this.step = step; }
    public void setLowerLimit(double lowerLimit) { this.lowerLimit = lowerLimit; }
    public void setUpperLimit(double upperLimit) { this.upperLimit = upperLimit; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setTotalClients(int totalClients) { this.totalClients = totalClients; }
    public void setTaskCreationTime(long taskCreationTime) { this.taskCreationTime = taskCreationTime; }
    
    public double getSegmentStart() {
        double segmentLength = (upperLimit - lowerLimit) / totalClients;
        return lowerLimit + clientId * segmentLength;
    }
    
    public double getSegmentEnd() {
        double segmentLength = (upperLimit - lowerLimit) / totalClients;
        if (clientId == totalClients - 1) {
            return upperLimit;
        }
        return lowerLimit + (clientId + 1) * segmentLength;
    }
    
    @Override
    public String toString() {
        return String.format("IntegralTask{clientId=%d, step=%.6f, segment=[%.6f, %.6f]}",
                            clientId, step, getSegmentStart(), getSegmentEnd());
    }
}
