package com.abhishek.mycalculator;

public class CalculationHistory {
    private String expression;
    private String result;
    private String timestamp;
    private long timeInMillis;

    public CalculationHistory(String expression, String result, String timestamp, long timeInMillis) {
        this.expression = expression;
        this.result = result;
        this.timestamp = timestamp;
        this.timeInMillis = timeInMillis;
    }

    // Getters
    public String getExpression() {
        return expression;
    }

    public String getResult() {
        return result;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    // Setters
    public void setExpression(String expression) {
        this.expression = expression;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }
}
