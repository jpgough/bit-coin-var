package com.jpgough.bitcoin;

public class AdviceResponse {
    private String adviceMessage;
    private String disclaimer;

    public AdviceResponse(String adviceMessage, String disclaimer) {
        this.adviceMessage = adviceMessage;
        this.disclaimer = disclaimer;
    }

    public String getAdviceMessage() {
        return adviceMessage;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}
