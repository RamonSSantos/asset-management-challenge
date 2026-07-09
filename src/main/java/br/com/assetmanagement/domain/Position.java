package br.com.assetmanagement.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Position {
    private PositionType type = PositionType.NONE;
    private int quantity;
    private BigDecimal totalCost = BigDecimal.ZERO;
    private BigDecimal realizedProfit = BigDecimal.ZERO;

    public PositionType getType() {
        return type;
    }

    public void setType(PositionType type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getRealizedProfit() {
        return realizedProfit;
    }
    
    public void addRealizedProfit(BigDecimal value) {
        realizedProfit = realizedProfit.add(value);
    }

    public BigDecimal getAveragePrice() {
        if (quantity == 0) {
            return BigDecimal.ZERO;
        }

        return totalCost.divide(
            BigDecimal.valueOf(quantity),
            10,
            RoundingMode.HALF_UP
        );
    }
}
