import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.assetmanagement.domain.Position;
import br.com.assetmanagement.domain.PositionType;
import br.com.assetmanagement.domain.Trade;
import br.com.assetmanagement.service.PositionProcessor;

public class PositionProcessorTest {
    private PositionProcessor processor = new PositionProcessor();

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    void shouldOpenLongPosition() {
        Position position = processor.process(List.of(TestDataFactory.buy(10, 20)), TODAY);

        assertEquals(PositionType.LONG, position.getType());
        assertEquals(10, position.getQuantity());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldOpenShortPosition() {
        Position position = processor.process(List.of(TestDataFactory.sell(10, 20)), TODAY);

        assertEquals(PositionType.SHORT, position.getType());
        assertEquals(10, position.getQuantity());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldCalculateWeightedAveragePrice() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20),
            TestDataFactory.buy(20, 25)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(30, position.getQuantity());
        assertEquals(BigDecimal.valueOf(23.3333333333).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldKeepAveragePriceAfterPartialSell() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(20, 20),
            TestDataFactory.sell(5, 25)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(15, position.getQuantity());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldCloseLongPosition() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(20, 20),
            TestDataFactory.sell(20, 25)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(PositionType.NONE, position.getType());
        assertEquals(0, position.getQuantity());
    }

    @Test
    void shouldReverseLongIntoShort() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(20, 20),
            TestDataFactory.sell(30, 20)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(10, position.getQuantity());
        assertEquals(PositionType.SHORT, position.getType());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldReverseShortIntoLong() {
        List<Trade> trades = List.of(
            TestDataFactory.sell(30, 20),
            TestDataFactory.buy(40, 20)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(10, position.getQuantity());
        assertEquals(PositionType.LONG, position.getType());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldCalculateRealizedProfitWhenClosingLongPosition() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20),
            TestDataFactory.sell(10, 30)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
        assertEquals(BigDecimal.valueOf(100).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());
    }

    @Test
    void shouldCalculateRealizedProfitWhenClosingShortPosition() {
        List<Trade> trades = List.of(
            TestDataFactory.sell(10, 20),
            TestDataFactory.buy(10, 15)
        );

        Position position = processor.process(trades, TODAY);

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
        assertEquals(BigDecimal.valueOf(50).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());
    }

    @Test
    void shouldIgnoreTradesAfterTargetDate() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20, LocalDate.of(2026, 6, 1)),
            TestDataFactory.buy(10, 19, LocalDate.of(2026, 6, 15)),
            TestDataFactory.buy(10, 18, LocalDate.of(2026, 6, 22))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 8));

        assertEquals(10, position.getQuantity());
        assertEquals(PositionType.LONG, position.getType());
        assertEquals(BigDecimal.valueOf(20).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldReturnEmptyPositionWhenTradeListIsEmpty() {
        List<Trade> trades = List.of();

        Position position = processor.process(trades, LocalDate.of(2026, 6, 8));

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
    }

    @Test
    void shouldReturnEmptyPositionWhenTargetDateIsBeforeFirstTrade() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20, LocalDate.of(2026, 6, 1))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 5, 29));

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
    }

    @Test
    void shouldProcessMultipleBuyTradesOnSameDay() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20, LocalDate.of(2026, 6, 1)),
            TestDataFactory.buy(10, 18, LocalDate.of(2026, 6, 1))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 1));

        assertEquals(20, position.getQuantity());
        assertEquals(PositionType.LONG, position.getType());
        assertEquals(BigDecimal.valueOf(19).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
    }

    @Test
    void shouldProcessMultipleSellTradesOnSameDay() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20, LocalDate.of(2026, 6, 1)),
            TestDataFactory.sell(5, 22, LocalDate.of(2026, 6, 2)),
            TestDataFactory.sell(5, 23, LocalDate.of(2026, 6, 2))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 2));

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
        assertEquals(BigDecimal.valueOf(25).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());
    }

    @Test
    void shouldClosePositionWhenSellingAllShares() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(10, 20, LocalDate.of(2026, 6, 1)),
            TestDataFactory.sell(5, 20, LocalDate.of(2026, 6, 23)),
            TestDataFactory.sell(5, 20, LocalDate.of(2026, 6, 30))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 30));

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
        assertEquals(BigDecimal.valueOf(0).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());
    }

    @Test
    void shouldCloseShortPositionWhenBuyingAllBorrowedShares() {
        List<Trade> trades = List.of(
            TestDataFactory.sell(10, 20, LocalDate.of(2026, 6, 10)),
            TestDataFactory.buy(10, 15, LocalDate.of(2026, 6, 17))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 17));

        assertEquals(0, position.getQuantity());
        assertEquals(PositionType.NONE, position.getType());
        assertEquals(BigDecimal.ZERO, position.getTotalCost());
        assertEquals(BigDecimal.valueOf(50).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());  
    }

    @Test
    void shouldReverseFromLongToShortWhenSellingMoreThanOwned() {
        List<Trade> trades = List.of(
            TestDataFactory.buy(20, 10, LocalDate.of(2026, 6, 10)),
            TestDataFactory.sell(30, 15, LocalDate.of(2026, 6, 17))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 17));

        assertEquals(10, position.getQuantity());
        assertEquals(PositionType.SHORT, position.getType());
        assertEquals(BigDecimal.valueOf(15).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
        assertTrue(BigDecimal.valueOf(150).compareTo(position.getTotalCost()) == 0);
        assertEquals(BigDecimal.valueOf(100).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());  
    }

    @Test
    void shouldReverseFromShortToLongWhenBuyingMoreThanShortPosition() {
        List<Trade> trades = List.of(
            TestDataFactory.sell(30, 20, LocalDate.of(2026, 6, 10)),
            TestDataFactory.buy(40, 15, LocalDate.of(2026, 6, 17))
        );

        Position position = processor.process(trades, LocalDate.of(2026, 6, 17));

        assertEquals(10, position.getQuantity());
        assertEquals(PositionType.LONG, position.getType());
        assertEquals(BigDecimal.valueOf(15).setScale(10, RoundingMode.HALF_UP), position.getAveragePrice());
        assertTrue(BigDecimal.valueOf(150).compareTo(position.getTotalCost()) == 0);
        assertEquals(BigDecimal.valueOf(150).setScale(10, RoundingMode.HALF_UP), position.getRealizedProfit());  
    }
}
