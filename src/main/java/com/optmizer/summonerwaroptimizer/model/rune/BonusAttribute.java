package com.optmizer.summonerwaroptimizer.model.rune;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.optmizer.summonerwaroptimizer.model.rune.EffectAggregationType.MULTIPLY;
import static com.optmizer.summonerwaroptimizer.model.rune.EffectAggregationType.SUM;

@Getter
@AllArgsConstructor
public enum BonusAttribute {

    FLAT_HIT_POINTS(MonsterAttribute.HIT_POINTS, 2448, 355, SUM),
    HIT_POINTS(MonsterAttribute.HIT_POINTS, 63, 8, MULTIPLY),
    FLAT_ATTACK(MonsterAttribute.ATTACK, 160, 16, SUM),
    ATTACK(MonsterAttribute.ATTACK, 63, 8, MULTIPLY),
    FLAT_DEFENSE(MonsterAttribute.DEFENSE, 160, 17, SUM),
    DEFENSE(MonsterAttribute.DEFENSE, 63, 8, MULTIPLY),
    SPEED(MonsterAttribute.SPEED, 42, 6, SUM),
    CRITICAl_RATE(MonsterAttribute.CRITICAL_RATE, 58, 6, SUM),
    CRITICAL_DAMAGE(MonsterAttribute.CRITICAL_DAMAGE, 80, 7, SUM),
    RESISTANCE(MonsterAttribute.RESISTANCE, 64, 8, SUM),
    ACCURACY(MonsterAttribute.ACCURACY, 64, 8, SUM);

    private final MonsterAttribute monsterAttribute;
    private final Integer maxMainStatBonus;
    private final Integer maxSubStatBonus;
    private final EffectAggregationType effectAggregationType;

}
