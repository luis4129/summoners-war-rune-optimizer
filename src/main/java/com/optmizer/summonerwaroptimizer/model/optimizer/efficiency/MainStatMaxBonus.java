package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public enum MainStatMaxBonus {

    FLAT_HIT_POINTS(BigDecimal.ZERO, BigDecimal.valueOf(804), BigDecimal.valueOf(1092), BigDecimal.valueOf(1380), BigDecimal.valueOf(1704), BigDecimal.valueOf(2088), BigDecimal.valueOf(2448)),
    HIT_POINTS(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(38), BigDecimal.valueOf(43), BigDecimal.valueOf(51), BigDecimal.valueOf(63)),

    FLAT_ATTACK(BigDecimal.ZERO, BigDecimal.valueOf(54), BigDecimal.valueOf(73), BigDecimal.valueOf(92), BigDecimal.valueOf(112), BigDecimal.valueOf(135), BigDecimal.valueOf(160)),
    ATTACK(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(38), BigDecimal.valueOf(43), BigDecimal.valueOf(51), BigDecimal.valueOf(63)),

    FLAT_DEFENSE(BigDecimal.ZERO, BigDecimal.valueOf(54), BigDecimal.valueOf(73), BigDecimal.valueOf(92), BigDecimal.valueOf(112), BigDecimal.valueOf(135), BigDecimal.valueOf(160)),
    DEFENSE(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(38), BigDecimal.valueOf(43), BigDecimal.valueOf(51), BigDecimal.valueOf(63)),

    SPEED(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(25), BigDecimal.valueOf(30), BigDecimal.valueOf(39), BigDecimal.valueOf(42)),

    CRITICAL_RATE(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(37), BigDecimal.valueOf(42), BigDecimal.valueOf(47), BigDecimal.valueOf(58)),
    CRITICAL_DAMAGE(BigDecimal.ZERO, BigDecimal.valueOf(19), BigDecimal.valueOf(37), BigDecimal.valueOf(43), BigDecimal.valueOf(57), BigDecimal.valueOf(65), BigDecimal.valueOf(80)),

    RESISTANCE(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(38), BigDecimal.valueOf(44), BigDecimal.valueOf(51), BigDecimal.valueOf(64)),
    ACCURACY(BigDecimal.ZERO, BigDecimal.valueOf(18), BigDecimal.valueOf(19), BigDecimal.valueOf(38), BigDecimal.valueOf(44), BigDecimal.valueOf(51), BigDecimal.valueOf(64));

    private final List<BigDecimal> maxGradeBonusList;

    private MainStatMaxBonus(BigDecimal... maxBonus) {
        this.maxGradeBonusList = List.of(maxBonus);
    }

    public static MainStatMaxBonus valueOf(BonusAttribute bonusAttribute) {
        return MainStatMaxBonus.valueOf(bonusAttribute.name());

    }
}
