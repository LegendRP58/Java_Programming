package com.mycompany.lab6;

import java.io.Serializable;

public class IntegralResult implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double partialSum;
    private int clientId;
    private long computationTime;
    private long totalClientTime;
    private long taskCreationTime;
    private long resultSendTime;
    private boolean success;
    private String errorMessage;
    
    public IntegralResult() {
        this.success = true;
    }
    
    public IntegralResult(double partialSum, int clientId, long computationTime) {
        this.partialSum = partialSum;
        this.clientId = clientId;
        this.computationTime = computationTime;
        this.success = true;
        this.errorMessage = null;
        this.totalClientTime = computationTime;
        this.taskCreationTime = 0;
        this.resultSendTime = 0;
    }
    
    public IntegralResult(int clientId, String errorMessage) {
        this.clientId = clientId;
        this.errorMessage = errorMessage;
        this.success = false;
        this.partialSum = 0;
        this.computationTime = 0;
        this.totalClientTime = 0;
        this.taskCreationTime = 0;
        this.resultSendTime = 0;
    }
    
    // Геттеры
    public double getPartialSum() { return partialSum; }
    public int getClientId() { return clientId; }
    public long getComputationTime() { return computationTime; }
    public long getTotalClientTime() { return totalClientTime; }
    public long getTaskCreationTime() { return taskCreationTime; }
    public long getResultSendTime() { return resultSendTime; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    
    // Сеттеры
    public void setPartialSum(double partialSum) { this.partialSum = partialSum; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public void setComputationTime(long computationTime) { this.computationTime = computationTime; }
    public void setTotalClientTime(long totalClientTime) { this.totalClientTime = totalClientTime; }
    public void setTaskCreationTime(long taskCreationTime) { this.taskCreationTime = taskCreationTime; }
    public void setResultSendTime(long resultSendTime) { this.resultSendTime = resultSendTime; }
    public void setSuccess(boolean success) { this.success = success; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    // Методы для получения времени в миллисекундах
    public double getComputationTimeMs() {
        return computationTime / 1_000_000.0;
    }
    
    public double getTotalClientTimeMs() {
        return totalClientTime / 1_000_000.0;
    }
    
    public double getNetworkTimeMs() {
        if (totalClientTime > 0 && computationTime > 0) {
            return (totalClientTime - computationTime) / 1_000_000.0;
        }
        return 0;
    }
    
    public double getTotalRoundTripTimeMs() {
        if (resultSendTime > 0 && taskCreationTime > 0) {
            return (resultSendTime - taskCreationTime) / 1_000_000.0;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("IntegralResult{clientId=%d, partialSum=%.6f, computationTime=%.3f ms, networkTime=%.3f ms}",
                                clientId, partialSum, getComputationTimeMs(), getNetworkTimeMs());
        } else {
            return String.format("IntegralResult{clientId=%d, success=false, error='%s'}",
                                clientId, errorMessage);
        }
    }
}