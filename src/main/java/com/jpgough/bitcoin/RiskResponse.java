package com.jpgough.bitcoin;

public class RiskResponse {
    private final int numberOfDays;
    private final double amount;
    private final double var;
    private final String formattedResponse;

    RiskResponse(int numberOfDays, double amount, double var, String formattedResponse) {
        this.numberOfDays = numberOfDays;
        this.amount = amount;
        this.var = var;
        this.formattedResponse = formattedResponse;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }


    public double getAmount() {
        return amount;
    }


    public double getVar() {
        return var;
    }

    public String getFormattedResponse() {
        return formattedResponse;
    }

    public String getDisclaimer() {
        return "This is a toy example, figures may be wildly inaccurate";
    }
}
