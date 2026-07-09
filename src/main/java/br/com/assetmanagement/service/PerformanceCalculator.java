package br.com.assetmanagement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import br.com.assetmanagement.domain.PortfolioResult;
import br.com.assetmanagement.domain.Position;
import br.com.assetmanagement.domain.PositionType;
import br.com.assetmanagement.util.MoneyUtil;

public class PerformanceCalculator {
    public PortfolioResult calculate(Position position, BigDecimal closingPrice) {
        if (position.getType() == PositionType.NONE) {
            return new PortfolioResult(BigDecimal.ZERO, MoneyUtil.round(position.getRealizedProfit()), BigDecimal.ZERO);
        }

        var quantity = BigDecimal.valueOf(position.getQuantity());
        var currentValue = closingPrice.multiply(quantity);

        var unrealized =
        position.getType() == PositionType.LONG
                ? closingPrice.subtract(position.getAveragePrice()).multiply(quantity)
                : position.getAveragePrice().subtract(closingPrice).multiply(quantity);

        var totalProfit = position.getRealizedProfit().add(unrealized);
        var percentage = BigDecimal.ZERO;

        if (position.getTotalCost().compareTo(BigDecimal.ZERO) > 0) {
            percentage = totalProfit.multiply(BigDecimal.valueOf(100))
                .divide(position.getTotalCost(), 6, RoundingMode.HALF_UP);
        }

        return new PortfolioResult(MoneyUtil.round(currentValue),
            MoneyUtil.round(totalProfit),
            MoneyUtil.round(percentage));
    }
}
