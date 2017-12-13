package com.jpgough.bitcoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.text.NumberFormat;
import java.util.Locale;

@RestController
public class BitCoinController {

    @Autowired
    private BitCoinVarCalculator bitCoinVarCalculator;

    @RequestMapping(value = "/", method= RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public RiskResponse calculateBitCoinUSDVar() {
        return calculateBitCoinUSDCustomVar(200);
    }

    @RequestMapping(value = "/", params = {"amount"}, method= RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public RiskResponse calculateBitCoinUSDCustomVar(@RequestParam(value="amount") double amount) {
        double VaR = bitCoinVarCalculator.calculateVar(amount);
        Locale usd = new Locale("en","US");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(usd);

        return new RiskResponse(10, amount ,VaR, currencyFormat.format(VaR));
    }

    @RequestMapping(value="/advice", method = RequestMethod.GET)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public AdviceResponse processAdviceRequest() {
        return new AdviceResponse("Forecast Average - Hold", "This is a toy service, advice is totally bogus");
    }
}
