package com.jpgough.bitcoin;

import com.codesnippets4all.json.parsers.JSONParser;
import com.codesnippets4all.json.parsers.JsonParserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BitCoinVarCalculator {

    public BitCoinVarCalculator() {
        JsonParserFactory factory = JsonParserFactory.getInstance();
        this.parser = factory.newJsonParser();
    }

    @Autowired
    private RestTemplate restTemplate;

    private final DateTimeFormatter formatToDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final JSONParser parser;

    public double calculateVar(double amount) {

        LocalDate endDate = LocalDate.now().minusDays(2);
        LocalDate beginDate = endDate.minusYears(1);

        //Move to injected dependency for testing
        Map<String, String> params = new HashMap<>();
        params.put("startDate", beginDate.format(formatToDate));
        params.put("endDate", endDate.format(formatToDate));
        String coinDeskResult = restTemplate.getForObject("https://api.coindesk.com/v1/bpi/historical/close.json?start={startDate}&end={endDate}",
                String.class, params);

        Map data = parser.parseJson(coinDeskResult);
        Map prices = (Map) data.get("bpi");

        double portfolioValue = amount * Double.parseDouble((String) prices.get(endDate.format(formatToDate)));

        LocalDate currentDate = beginDate;

        List<Double> dailyBTCPrices = new ArrayList<>();
        while (currentDate.isBefore(endDate)) {  // looking up each date in the parsed data and getting the price
            dailyBTCPrices.add(Double.parseDouble((String) prices.get(currentDate.format(formatToDate))));
            currentDate = currentDate.plusDays(1);
        }

        List<Double> dailyBTCPriceEvolution = new ArrayList<>(); // the list of daily BTC price evolution, named as return
        for (int i = 1; i <= dailyBTCPrices.size() - 1; i++) { // calculating daily return and adding it to the return list
            dailyBTCPriceEvolution.add(dailyBTCPrices.get(i)/dailyBTCPrices.get(i-1)*100 - 100);
        }

        double sumOfReturn = dailyBTCPriceEvolution.stream().reduce(0.0, Double::sum);

        double averageOfReturn = sumOfReturn/dailyBTCPriceEvolution.size(); // calculating average of returns
        double std_dev = getStdDev(dailyBTCPriceEvolution, averageOfReturn); // standard deviation using the method
        double annualVolatility = std_dev * Math.sqrt(365d); // transforming standard deviation in annual volatility
        return annualVolatility/100 * Math.sqrt(10/365d) * portfolioValue * 1.65;
    }


    private double getStdDev(List<Double> priceEvolution, double average) {
        return Math.sqrt(priceEvolution.stream().map(evolution -> Math.pow(evolution - average, 2)/priceEvolution.size())
                .reduce(0.0, Double::sum));
    }

}
