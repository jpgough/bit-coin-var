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

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
@RestController
public class Application {

    @RequestMapping("/")
    @ResponseBody
    public String calculateBitCoinUSDVar() throws MalformedURLException {
        return calculateBitCoinUSDCustomVar(200);
//
//
//        Calendar end = Calendar.getInstance();
//        end.add(Calendar.DAY_OF_MONTH, -2); // we substract 2 days because the data may not be updated to latest day
//
//        Calendar date = Calendar.getInstance();
//        date.add(Calendar.YEAR, -1); // we substract depending on the period we want to calculate the volatility for
//
//
//        System.out.println("Begin: " + df.format(date.getTime()));
//        System.out.println("End: " + df.format(end.getTime()));
//
//        try {
//            // creating the URL using StringBuffer
//            StringBuffer urlBuffer = new StringBuffer();
//
//            URL url = new URL(urlBuffer.toString());
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null ) {
//                text.append(line);
//            }
//            reader.close();
//        } catch (IOException e ) {
//            e.printStackTrace();
//        }
//
//        //parsing the JSON data
//        JsonParserFactory factory = JsonParserFactory.getInstance();
//        JSONParser parser = factory.newJsonParser();
//        Map data = parser.parseJson(text.toString());
//        Map prices = (Map) data.get("bpi");

        // calculating portfolio data using latest prices
//        portfolioValue = 200 * Double.parseDouble((String) prices.get(df.format(end.getTime())));
//
//        while (date.before(end)) {  // looking up each date in the parsed data and getting the price
//            dailyBTCPrices.add(Double.parseDouble((String) prices.get(df.format(date.getTime()))));
//            date.add(Calendar.DAY_OF_MONTH, 1);
//        }
//
//        for (int i = 1; i <= dailyBTCPrices.size() - 1; i++) { // calculating daily return and adding it to the return list
//            dailyBTCPriceEvolution.add(dailyBTCPrices.get(i)/dailyBTCPrices.get(i-1)*100 - 100);
//        }
//
//        for (Double returnValue : dailyBTCPriceEvolution) { // getting the sum of returns
//            sumOfReturn += returnValue;
//        }
//
//        averageOfReturn = sumOfReturn/dailyBTCPriceEvolution.size(); // calculating average of returns
//        std_dev = getStdDev(dailyBTCPriceEvolution, averageOfReturn); // standard deviation using the method
//        annualVolatility = std_dev * Math.sqrt(365d); // transforming standard deviation in annual volatility
//        VaR = annualVolatility/100 * Math.sqrt(10/365d) * portfolioValue * 1.65;
//
//        Locale usd = new Locale("en","US");
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(usd);
    //return "";
        //return "<h3>Value at risk for 200 BTC during next 10 days equals " + currencyFormat.format(VaR) + "</h3>";
    }

    @RequestMapping(value = "/", params = {"amount"})
    @ResponseBody
    public String calculateBitCoinUSDCustomVar(@RequestParam(value="amount") double amount) throws MalformedURLException {
        double VaR = calculateVar(amount);
        Locale usd = new Locale("en","US");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(usd);

        return "<h3>Value at risk for " + amount + " BTC during next 10 days equals " + currencyFormat.format(VaR) + "</h3>";
    }

    private double calculateVar(double amount) throws MalformedURLException {
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
