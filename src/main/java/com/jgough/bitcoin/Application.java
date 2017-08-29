package com.jgough.bitcoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.*;

@SpringBootApplication
@RestController
public class Application {

    @Autowired
    private BitCoinVarCalculator bitCoinVarCalculator;

    @RequestMapping("/")
    @ResponseBody
    public String calculateBitCoinUSDVar() {
        return calculateBitCoinUSDCustomVar(200);
    }

    @RequestMapping(value = "/", params = {"amount"})
    @ResponseBody
    public String calculateBitCoinUSDCustomVar(@RequestParam(value="amount") double amount) {
        double VaR = bitCoinVarCalculator.calculateVar(amount);
        Locale usd = new Locale("en","US");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(usd);

        return "<h3>Value at risk for " + amount + " BTC during next 10 days equals " + currencyFormat.format(VaR) + "</h3>";
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
