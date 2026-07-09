import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.assetmanagement.domain.PortfolioResult;
import br.com.assetmanagement.domain.Position;
import br.com.assetmanagement.domain.Quote;
import br.com.assetmanagement.domain.Trade;
import br.com.assetmanagement.service.PerformanceCalculator;
import br.com.assetmanagement.service.PortfolioService;
import br.com.assetmanagement.service.PositionProcessor;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {
    @Mock
    private PositionProcessor positionProcessor;

    @Mock
    private PerformanceCalculator performanceCalculator;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldCalculatePortfolioUsingProcessorAndCalculator() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2024, 1, 10);
        List<Trade> trades = List.of();

        List<Quote> quotes = List.of(new Quote(targetDate, "ABCD3", BigDecimal.valueOf(15)));
        Map<LocalDate, Quote> quoteMap = quotes.stream()
            .collect(Collectors.toMap(Quote::getDate, Function.identity()));

        Position position = new Position();

        PortfolioResult expected =
                new PortfolioResult(BigDecimal.valueOf(300), BigDecimal.valueOf(100), BigDecimal.valueOf(50));

        when(positionProcessor.process(trades, targetDate)).thenReturn(position);
        when(performanceCalculator.calculate(position, BigDecimal.valueOf(15))).thenReturn(expected);

        // Act
        PortfolioResult result = portfolioService.calculatePortfolio(trades, quoteMap, targetDate);

        // Assert
        assertSame(expected, result);

        verify(positionProcessor, times(1)).process(trades, targetDate);
        verify(performanceCalculator, times(1)).calculate(position, BigDecimal.valueOf(15));

        verifyNoMoreInteractions(positionProcessor, performanceCalculator);
    }

    @Test
    void shouldThrowExceptionWhenQuoteIsNotFound() {
        List<Trade> trades = List.of();
        Map<LocalDate, Quote> quoteMap = Map.of();
        LocalDate date = LocalDate.now();

        assertThrows(IllegalArgumentException.class,
                () -> portfolioService.calculatePortfolio(trades, quoteMap, date));
    }

    @Test
    void shouldUseClosingPriceForTargetDate() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2024, 1, 10);

        Map<LocalDate, Quote> quotes =
                Map.of(
                    LocalDate.of(2024,1,9), new Quote(LocalDate.of(2024,1,9), "ABCD3", BigDecimal.valueOf(12)),
                    LocalDate.of(2024,1,10), new Quote(targetDate, "ABCD3", BigDecimal.valueOf(15)),
                    LocalDate.of(2024,1,11), new Quote(LocalDate.of(2024,1,11), "ABCD3", BigDecimal.valueOf(18))
                );

        Position position = new Position();
        PortfolioResult expected = new PortfolioResult();

        when(positionProcessor.process(any(), eq(targetDate))).thenReturn(position);
        when(performanceCalculator.calculate(position,BigDecimal.valueOf(15))).thenReturn(expected);

        // Act
        portfolioService.calculatePortfolio(List.of(), quotes, targetDate);

        // Assert
        verify(performanceCalculator).calculate(position, BigDecimal.valueOf(15));
    }

    @Test
    void shouldHandleEmptyTradeList() {
        // Arrange
        LocalDate targetDate = LocalDate.of(2024, 1, 10);
        List<Trade> trades = Collections.emptyList();

        Quote quote = new Quote(targetDate, "ABCD3", BigDecimal.valueOf(15));
        Map<LocalDate, Quote> quotes = Map.of(targetDate, quote);

        Position emptyPosition = new Position();

        PortfolioResult expected = new PortfolioResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(positionProcessor.process(trades, targetDate))
                .thenReturn(emptyPosition);

        when(performanceCalculator.calculate(emptyPosition, BigDecimal.valueOf(15)))
                .thenReturn(expected);

        // Act
        PortfolioResult result = portfolioService.calculatePortfolio(trades, quotes, targetDate);

        // Assert
        assertEquals(expected, result);

        verify(positionProcessor).process(trades, targetDate);
        verify(performanceCalculator).calculate(emptyPosition, BigDecimal.valueOf(15));
    }
}