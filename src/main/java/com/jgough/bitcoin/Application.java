package com.jgough.bitcoin;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/")
    @ResponseBody
    public String calculateBitCoinUSDVar() {
        return calculateBitCoinUSDCustomVar(200);
    }

    @RequestMapping(value = "/", params = {"amount"})
    @ResponseBody
    public String calculateBitCoinUSDCustomVar(@RequestParam(value="amount") double amount) {
        double VaR = calculateVar(amount);
        Locale usd = new Locale("en","US");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(usd);

        return "<h3>Value at risk for " + amount + " BTC during next 10 days equals " + currencyFormat.format(VaR) + "</h3>";
    }

    private double calculateVar(double amount) {
        List<Double> dailyBTCPrices = new ArrayList<>();  // the list of daily BTC prices
        List<Double> dailyBTCPriceEvolution = new ArrayList<>(); // the list of daily BTC price evolution, named as return

        LocalDate endDate = LocalDate.now().minusDays(2);
        LocalDate beginDate = endDate.minusYears(1);

        DateTimeFormatter formatToDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //Move to injected dependency for testing
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = new HashMap<>();
        params.put("startDate", beginDate.format(formatToDate));
        params.put("endDate", endDate.format(formatToDate));
        String coinDeskResult = restTemplate.getForObject("https://api.coindesk.com/v1/bpi/historical/close.json?start={startDate}&end={endDate}",
                String.class, params);

        JsonParserFactory factory = JsonParserFactory.getInstance();
        JSONParser parser = factory.newJsonParser();
        Map data = parser.parseJson(coinDeskResult);
        Map prices = (Map) data.get("bpi");

        double portfolioValue = amount * Double.parseDouble((String) prices.get(endDate.format(formatToDate)));

        LocalDate currentDate = beginDate;

        while (currentDate.isBefore(endDate)) {  // looking up each date in the parsed data and getting the price
            dailyBTCPrices.add(Double.parseDouble((String) prices.get(currentDate.format(formatToDate))));
            currentDate = currentDate.plusDays(1);
        }

        for (int i = 1; i <= dailyBTCPrices.size() - 1; i++) { // calculating daily return and adding it to the return list
            dailyBTCPriceEvolution.add(dailyBTCPrices.get(i)/dailyBTCPrices.get(i-1)*100 - 100);
        }

        double sumOfReturn = 0;

        for (Double priceEvolution : dailyBTCPriceEvolution) { // getting the sum of returns
           sumOfReturn += priceEvolution;
        }

        double averageOfReturn = sumOfReturn/dailyBTCPriceEvolution.size(); // calculating average of returns
        double std_dev = getStdDev(dailyBTCPriceEvolution, averageOfReturn); // standard deviation using the method
        double annualVolatility = std_dev * Math.sqrt(365d); // transforming standard deviation in annual volatility
        return annualVolatility/100 * Math.sqrt(10/365d) * portfolioValue * 1.65;
    }


    private double getStdDev(List<Double> priceEvolution, double average) {
        Double sumOfDiff = 0d;
        for (Double returnValue : priceEvolution) {
            sumOfDiff += Math.pow(returnValue - average, 2)/priceEvolution.size();  // formula of standard deviation
        }
        return Math.sqrt(sumOfDiff);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
