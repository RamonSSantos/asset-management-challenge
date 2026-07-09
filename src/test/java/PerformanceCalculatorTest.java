import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.assetmanagement.domain.PortfolioResult;
import br.com.assetmanagement.domain.Position;
import br.com.assetmanagement.domain.PositionType;
import br.com.assetmanagement.service.PerformanceCalculator;

public class PerformanceCalculatorTest {
    private PerformanceCalculator calculator = new PerformanceCalculator();

    @Test
    void shouldCalculatePerformanceForLongPosition() {
        Position position = longPosition(20, 10, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(15));

        assertEquals(BigDecimal.valueOf(300.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldCalculateLossForLongPosition() {
        Position position = longPosition(20, 20, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(15));

        assertEquals(BigDecimal.valueOf(300.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(-100.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(-25.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldCalculateProfitForShortPosition() {
        Position position = shortPosition(20, 20, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(15));

        assertEquals(BigDecimal.valueOf(300.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(25.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldCalculateLossForShortPosition() {
        Position position = shortPosition(20, 20, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(25));

        assertEquals(BigDecimal.valueOf(500.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(-100.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(-25.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldIncludeRealizedProfitInPerformanceCalculation() {
        Position position = longPosition(10, 10, 80);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(12));

        assertEquals(BigDecimal.valueOf(120.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldReturnZeroValuesForEmptyPortfolio() {
        Position position = new Position();

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(10));

        assertEquals(BigDecimal.ZERO, result.getCurrentValue());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getProfit());
        assertEquals(BigDecimal.ZERO, result.getProfitPercentage());
    }

    @Test
    void shouldReturnZeroProfitWhenClosingPriceEqualsAveragePrice() {
        Position position = longPosition(10, 10, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(10));

        assertEquals(BigDecimal.valueOf(100).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getProfit());
        assertEquals(BigDecimal.ZERO.setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldHandleNegativeRealizedProfit() {
        Position position = longPosition(5, 20, -25);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(25));

        assertEquals(BigDecimal.ZERO.setScale(2), result.getProfit());
    }

    @Test
    void shouldCalculatePerformanceForSingleSharePosition() {
        Position position = longPosition(1, 10, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(15));

        assertEquals(BigDecimal.valueOf(15.00).setScale(2), result.getCurrentValue());
        assertEquals(BigDecimal.valueOf(5.00).setScale(2), result.getProfit());
        assertEquals(BigDecimal.valueOf(50.00).setScale(2), result.getProfitPercentage());
    }

    @Test
    void shouldRoundValuesToTwoDecimalPlaces() {
        Position position = longPosition(3, 8.3333333333, 0);

        PortfolioResult result = calculator.calculate(position, BigDecimal.valueOf(9.87654321));

        assertEquals(new BigDecimal("29.63"), result.getCurrentValue());
        assertEquals(new BigDecimal("4.63"), result.getProfit());
        assertEquals(new BigDecimal("18.52"),result.getProfitPercentage());
    }

    private Position longPosition(int quantity, double averagePrice, double realized) {
        Position p = new Position();

        p.setType(PositionType.LONG);
        p.setQuantity(quantity);
        p.setTotalCost(BigDecimal.valueOf(quantity * averagePrice));
        p.addRealizedProfit(BigDecimal.valueOf(realized));

        return p;
    }

    private Position shortPosition(int quantity, double averagePrice, double realized) {
        Position p = new Position();
        
        p.setType(PositionType.SHORT);
        p.setQuantity(quantity);
        p.setTotalCost(BigDecimal.valueOf(quantity * averagePrice));
        p.addRealizedProfit(BigDecimal.valueOf(realized));

        return p;
    }
}
