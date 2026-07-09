package br.com.assetmanagement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.assetmanagement.domain.PortfolioResult;
import br.com.assetmanagement.domain.Quote;
import br.com.assetmanagement.domain.Trade;

public class PortfolioService {
    private final PositionProcessor positionProcessor;
    private final PerformanceCalculator performanceCalculator;

    public PortfolioService(PositionProcessor positionProcessor, PerformanceCalculator performanceCalculator) {
        this.positionProcessor = positionProcessor;
        this.performanceCalculator = performanceCalculator;
    }

    /** 
     * Calcula a posição e o rendimento da carteira para uma data específica. 
     * 
     * @param trades operações realizadas 
     * @param quotes cotações históricas 
     * @param targetDate data desejada 
     * @return resultado da carteira 
     */
    public PortfolioResult calculatePortfolio(List<Trade> trades, 
        Map<LocalDate, Quote> quotes, LocalDate targetDate) {
        Objects.requireNonNull(trades, "Trades cannot be null.");
        Objects.requireNonNull(quotes, "Quotes cannot be null.");
        Objects.requireNonNull(targetDate, "Target date cannot be null.");

        var position = positionProcessor.process(trades, targetDate);

        var quote = findQuote(quotes, targetDate);

        return performanceCalculator.calculate(position, quote.getClosePrice());
    }

    private Quote findQuote(Map<LocalDate, Quote> quotes, LocalDate targetDate) {
        var quote = quotes.get(targetDate);
        if (quote == null) {
            throw new IllegalArgumentException("No closing quote found for date: %s".formatted(targetDate.toString()));
        }

        return quote;
    }
}
