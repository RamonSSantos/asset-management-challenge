package br.com.assetmanagement.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import br.com.assetmanagement.domain.Operationtype;
import br.com.assetmanagement.domain.Position;
import br.com.assetmanagement.domain.PositionType;
import br.com.assetmanagement.domain.Trade;

public class PositionProcessor {
    public Position process(List<Trade> trades, LocalDate targetDate) {
        var position = new Position();

        trades.stream().filter(trade -> !trade.getDate().isAfter(targetDate))
        .sorted(Comparator.comparing(Trade::getDate))
        .forEach(trade -> processTrade(position, trade));

        return position;
    }

    private void processTrade(Position position, Trade trade) {
        if (trade.getOperationType() == Operationtype.BUY) {
            processBuy(position, trade);
        } else {
            processSell(position, trade);
        }

        normalize(position);
    }

    private void processBuy(Position position, Trade trade) {
        switch (position.getType()) {
            case NONE -> openLong(position, trade);
            case LONG -> increaseLong(position, trade);
            case SHORT -> closeShort(position, trade);
        }
    }

    private void processSell(Position position, Trade trade) {
        switch (position.getType()) {
            case NONE -> openShort(position, trade);
            case LONG -> closeLong(position, trade);
            case SHORT -> increaseShort(position, trade);
        }
    }

    private void openLong(Position position, Trade trade) {
        position.setType(PositionType.LONG);
        position.setQuantity(trade.getQuantity());
        position.setTotalCost(trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity())));
    }

    private void openShort(Position position, Trade trade) {
        position.setType(PositionType.SHORT);
        position.setQuantity(trade.getQuantity());
        position.setTotalCost(trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity())));
    }

    private void increaseLong(Position position, Trade trade) {
        var newCost = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        var totalCost = position.getTotalCost().add(newCost);
        int totalQuantity = position.getQuantity() + trade.getQuantity();

        position.setTotalCost(totalCost);
        position.setQuantity(totalQuantity);
    }

    private void increaseShort(Position position, Trade trade) {
        var soldValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        var totalCost = position.getTotalCost().subtract(soldValue);
        int totalQuantity = position.getQuantity() + trade.getQuantity();

        position.setTotalCost(totalCost);
        position.setQuantity(totalQuantity);
    }

    private void closeLong(Position position, Trade trade) {
        int closingQuantity = Math.min(position.getQuantity(), trade.getQuantity());

        var realized = trade.getPrice().subtract(position.getAveragePrice()).multiply(BigDecimal.valueOf(closingQuantity));

        position.addRealizedProfit(realized);

        var removedCost = position.getAveragePrice().multiply(BigDecimal.valueOf(closingQuantity));
            position.setTotalCost(position.getTotalCost().subtract(removedCost));

        int remaining = position.getQuantity() - closingQuantity;
        if (remaining > 0) {
            position.setQuantity(remaining);
        } else {
            clear(position);
        }

        int shortQuantity = trade.getQuantity() - closingQuantity;
        if (shortQuantity > 0) {
            openShort(position, new Trade(trade.getDate(), trade.getTicker(), Operationtype.SELL, shortQuantity, trade.getPrice()));
        }
    }

    private void closeShort(Position position, Trade trade) {
        int currentQuantity = position.getQuantity();
        int soldQuantity = trade.getQuantity();

        int closingQuantity = Math.min(currentQuantity, soldQuantity);

        var profit = position.getAveragePrice().subtract(trade.getPrice()).multiply(BigDecimal.valueOf(closingQuantity));

        position.addRealizedProfit(profit);

        int remaining = currentQuantity - closingQuantity;
        if (remaining > 0) {
            position.setQuantity(remaining);

            var removedCost = position.getAveragePrice().multiply(BigDecimal.valueOf(closingQuantity));
            position.setTotalCost(position.getTotalCost().subtract(removedCost));

            return;
        }

        int longQuantity = soldQuantity - currentQuantity;
        if (longQuantity > 0) {
            openLong(position, new Trade(trade.getDate(), trade.getTicker(), Operationtype.BUY, longQuantity, trade.getPrice()));
            
            return;
        }

        clear(position);
    }
    
    private void clear(Position position) {
        position.setType(PositionType.NONE);
        position.setQuantity(0);
        position.setTotalCost(BigDecimal.ZERO);
    }

    private void normalize(Position position) {
        if (position.getQuantity() == 0) {
            position.setType(PositionType.NONE);
        }
    }
}
