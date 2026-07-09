import java.math.BigDecimal;
import java.time.LocalDate;

import br.com.assetmanagement.domain.Operationtype;
import br.com.assetmanagement.domain.Trade;

public final class TestDataFactory {
    private static final LocalDate TODAY = LocalDate.now();

    public static Trade buy(int quantity, double price) {
        return buy(quantity, price, TODAY);
    }

    public static Trade buy(int quantity, double price, LocalDate date) {
        return new Trade(
            date,
            "ABCD3",
            Operationtype.BUY,
            quantity,
            BigDecimal.valueOf(price));
    }

    public static Trade sell(int quantity, double price) {
        return sell(quantity, price, TODAY);
    }

    public static Trade sell(int quantity, double price, LocalDate date) {
        return new Trade(
            date,
            "ABCD3",
            Operationtype.SELL,
            quantity,
            BigDecimal.valueOf(price));
    }
}
