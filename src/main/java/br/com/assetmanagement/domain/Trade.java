package br.com.assetmanagement.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Trade {
    private final LocalDate date;
    private final String ticker;
    private final Operationtype operationType;
    private final int quantity;
    private final BigDecimal price;
    
    public Trade(LocalDate date, String ticker, Operationtype operationType, int quantity, BigDecimal price) {
        this.date = date;
        this.ticker = ticker;
        this.operationType = operationType;
        this.quantity = quantity;
        this.price = price;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTicker() {
        return ticker;
    }

    public Operationtype getOperationType() {
        return operationType;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
