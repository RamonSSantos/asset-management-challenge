package br.com.assetmanagement.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Quote {
    private final LocalDate date;
    private final String ticker;
    private final BigDecimal closePrice;

    public Quote(LocalDate date, String ticker, BigDecimal closePrice) {
        this.date = date;
        this.ticker = ticker;
        this.closePrice = closePrice;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTicker() {
        return ticker;
    }
    
    public BigDecimal getClosePrice() {
        return closePrice;
    }
}
