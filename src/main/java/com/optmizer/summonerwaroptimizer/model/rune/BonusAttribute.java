package com.optmizer.summonerwaroptimizer.model.rune;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

import static com.optmizer.summonerwaroptimizer.model.rune.EffectAggregationType.MULTIPLY;
import static com.optmizer.summonerwaroptimizer.model.rune.EffectAggregationType.SUM;

@Getter
@AllArgsConstructor
public enum BonusAttribute {

    FLAT_HIT_POINTS(MonsterAttribute.HIT_POINTS,
        SUM,
        BigDecimal.valueOf(2448),
        BigDecimal.valueOf(375),
        BigDecimal.valueOf(550),
        BigDecimal.valueOf(580),
        BigDecimal.valueOf(435),
        BigDecimal.valueOf(610),
        BigDecimal.valueOf(640)),
    HIT_POINTS(MonsterAttribute.HIT_POINTS,
        MULTIPLY,
        BigDecimal.valueOf(63),
        BigDecimal.valueOf(8),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(13),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(12),
        BigDecimal.valueOf(15)),

    FLAT_ATTACK(MonsterAttribute.ATTACK,
        SUM,
        BigDecimal.valueOf(160),
        BigDecimal.valueOf(20),
        BigDecimal.valueOf(30),
        BigDecimal.valueOf(40),
        BigDecimal.valueOf(24),
        BigDecimal.valueOf(34),
        BigDecimal.valueOf(44)),

    ATTACK(MonsterAttribute.ATTACK,
        MULTIPLY,
        BigDecimal.valueOf(63),
        BigDecimal.valueOf(8),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(13),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(12),
        BigDecimal.valueOf(15)),

    FLAT_DEFENSE(MonsterAttribute.DEFENSE,
        SUM,
        BigDecimal.valueOf(160),
        BigDecimal.valueOf(20),
        BigDecimal.valueOf(30),
        BigDecimal.valueOf(40),
        BigDecimal.valueOf(24),
        BigDecimal.valueOf(34),
        BigDecimal.valueOf(44)),

    DEFENSE(MonsterAttribute.DEFENSE,
        MULTIPLY,
        BigDecimal.valueOf(63),
        BigDecimal.valueOf(8),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(13),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(12),
        BigDecimal.valueOf(15)),

    SPEED(MonsterAttribute.SPEED,
        SUM,
        BigDecimal.valueOf(42),
        BigDecimal.valueOf(6),
        BigDecimal.valueOf(5),
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(7),
        BigDecimal.valueOf(6),
        BigDecimal.valueOf(11)),

    CRITICAL_RATE(MonsterAttribute.CRITICAL_RATE,
        SUM,
        BigDecimal.valueOf(58),
        BigDecimal.valueOf(6),
        BigDecimal.ZERO,
        BigDecimal.valueOf(9),
        BigDecimal.valueOf(7),
        BigDecimal.ZERO,
        BigDecimal.valueOf(10)),

    CRITICAL_DAMAGE(MonsterAttribute.CRITICAL_DAMAGE,
        SUM,
        BigDecimal.valueOf(80),
        BigDecimal.valueOf(7),
        BigDecimal.ZERO,
        BigDecimal.valueOf(10),
        BigDecimal.valueOf(9),
        BigDecimal.ZERO,
        BigDecimal.valueOf(12)),

    RESISTANCE(MonsterAttribute.RESISTANCE,
        SUM,
        BigDecimal.valueOf(64),
        BigDecimal.valueOf(8),
        BigDecimal.ZERO,
        BigDecimal.valueOf(11),
        BigDecimal.valueOf(10),
        BigDecimal.ZERO,
        BigDecimal.valueOf(13)),

    ACCURACY(MonsterAttribute.ACCURACY,
        SUM,
        BigDecimal.valueOf(64),
        BigDecimal.valueOf(8),
        BigDecimal.ZERO,
        BigDecimal.valueOf(11),
        BigDecimal.valueOf(10),
        BigDecimal.ZERO,
        BigDecimal.valueOf(13));

    private final MonsterAttribute monsterAttribute;
    private final EffectAggregationType effectAggregationType;
    private final BigDecimal maxMainStatBonus;
    private final BigDecimal maxSubStatBonus;
    private final BigDecimal maxGrindValue;
    private final BigDecimal maxEnchantmentValue;
    private final BigDecimal maxAncientStart;
    private final BigDecimal maxAncientGrind;
    private final BigDecimal maxAncientEnchantment;

    public BigDecimal getMaxGrindedSubStatBonus() {
        return maxAncientStart.add(maxAncientGrind);
    }

    public BigDecimal getMaxEnchantmentSubStatBonus() {
        return maxAncientEnchantment.add(maxAncientGrind);
    }

    public BigDecimal getFullyMaxedSubStatBonus() {
        return maxSubStatBonus.multiply(BigDecimal.valueOf(4)).add(maxAncientStart).add(maxAncientGrind);
    }

}
