package br.com.assetmanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyUtil {

    private MoneyUtil() {
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
