package br.com.assetmanagement.domain;

import java.math.BigDecimal;

public class PortfolioResult {
    private final BigDecimal currentValue;
    private final BigDecimal profit;
    private final BigDecimal profitPercentage;
    
    public PortfolioResult() {
        this.currentValue = BigDecimal.ZERO;
        this.profit = BigDecimal.ZERO;
        this.profitPercentage = BigDecimal.ZERO;
    }

    public PortfolioResult(BigDecimal currentValue, BigDecimal profit, BigDecimal profitPercentage) {
        this.currentValue = currentValue;
        this.profit = profit;
        this.profitPercentage = profitPercentage;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }
}
