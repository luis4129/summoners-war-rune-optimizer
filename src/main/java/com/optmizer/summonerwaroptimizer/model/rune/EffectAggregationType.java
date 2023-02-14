package com.optmizer.summonerwaroptimizer.model.rune;

import java.math.BigDecimal;
import java.math.RoundingMode;

public enum EffectAggregationType {

    NONE {
        @Override
        public BigDecimal calculate(Integer baseValue, Integer bonusValue) {
            return null;
        }
    },
    SUM {
        @Override
        public BigDecimal calculate(Integer baseValue, Integer bonusValue) {
            return BigDecimal.valueOf(bonusValue);
        }
    },
    MULTIPLY {
        @Override
        public BigDecimal calculate(Integer baseValue, Integer bonusValue) {
            return BigDecimal.valueOf(baseValue)
                .multiply(BigDecimal.valueOf(bonusValue))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_DOWN);
        }
    };

    public abstract BigDecimal calculate(Integer baseValue, Integer bonusValue);

}
