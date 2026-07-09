package br.com.assetmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import br.com.assetmanagement.domain.Operationtype;
import br.com.assetmanagement.domain.PortfolioResult;
import br.com.assetmanagement.domain.Quote;
import br.com.assetmanagement.domain.Trade;
import br.com.assetmanagement.service.PerformanceCalculator;
import br.com.assetmanagement.service.PortfolioService;
import br.com.assetmanagement.service.PositionProcessor;

/**
 * Example entry point demonstrating how to use the portfolio calculation.
 * The business logic is validated through unit tests.
 */
public class Main {
    public static void main(String[] args) {
        PositionProcessor processor = new PositionProcessor();
        PerformanceCalculator calculator = new PerformanceCalculator();
        PortfolioService service = new PortfolioService(processor, calculator);

        List<Trade> trades = List.of(
            new Trade(LocalDate.of(2026, 6, 1), "ABCD3", Operationtype.BUY, 20, BigDecimal.valueOf(10))
        );

        Map<LocalDate, Quote> quotes = Map.of(
            LocalDate.of(2026, 6, 10), new Quote(LocalDate.of(2026, 6, 10), "ABCD3", BigDecimal.valueOf(12))
        );

        PortfolioResult result = service.calculatePortfolio(trades, quotes, LocalDate.of(2026, 6, 10));

        System.out.println(result.getCurrentValue());
        System.out.println(result.getProfit());
        System.out.println(result.getProfitPercentage());
    }
}