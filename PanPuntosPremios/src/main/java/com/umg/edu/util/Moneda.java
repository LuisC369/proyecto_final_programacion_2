package com.umg.edu.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class Moneda {

    private Moneda() {
    }

    public static String formato(BigDecimal valor) {
        if (valor == null) {
            return "Q 0.00";
        }
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Q " + formato.format(valor.setScale(2, RoundingMode.HALF_UP));
    }
}
