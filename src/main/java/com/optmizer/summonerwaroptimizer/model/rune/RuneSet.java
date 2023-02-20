package com.optmizer.summonerwaroptimizer.model.rune;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.optmizer.summonerwaroptimizer.model.rune.EffectAggregationType.*;

@Getter
@AllArgsConstructor
public enum RuneSet {

    ANY(0, MonsterAttribute.NONE, 0, NONE),
    ENERGY(2, MonsterAttribute.HIT_POINTS, 15, MULTIPLY),
    FATAL(4, MonsterAttribute.ATTACK, 35, MULTIPLY),
    BLADE(2, MonsterAttribute.CRITICAL_RATE, 12, SUM),
    RAGE(4, MonsterAttribute.CRITICAL_DAMAGE, 40, SUM),
    SWIFT(4, MonsterAttribute.SPEED, 25, MULTIPLY),
    FOCUS(2, MonsterAttribute.ACCURACY, 20, SUM),
    GUARD(2, MonsterAttribute.DEFENSE, 15, MULTIPLY),
    ENDURE(2, MonsterAttribute.RESISTANCE, 20, SUM),
    VIOLENT(4, MonsterAttribute.NONE, 0, NONE),
    WILL(2, MonsterAttribute.NONE, 0, NONE),
    NEMESIS(2, MonsterAttribute.NONE, 0, NONE),
    SHIELD(2, MonsterAttribute.NONE, 0, NONE),
    REVENGE(2, MonsterAttribute.NONE, 0, NONE),
    DESPAIR(4, MonsterAttribute.NONE, 0, NONE),
    VAMPIRE(4, MonsterAttribute.NONE, 0, NONE),
    DESTROY(2, MonsterAttribute.NONE, 0, NONE),
    FIGHT(2, MonsterAttribute.ATTACK, 8, MULTIPLY),
    DETERMINATION(2, MonsterAttribute.DEFENSE, 8, MULTIPLY),
    ENHANCE(2, MonsterAttribute.HIT_POINTS, 8, MULTIPLY),
    ACCURACY(2, MonsterAttribute.ACCURACY, 10, SUM),
    TOLERANCE(2, MonsterAttribute.RESISTANCE, 10, SUM);

    private final Integer requirement;
    private final MonsterAttribute attribute;
    private final Integer bonusValue;
    private final EffectAggregationType effectAggregationType;

    public boolean hasAttributeBonus() {
        return !attribute.equals(MonsterAttribute.NONE);
    }

    public Integer calculateActiveEffects(Integer runesCount) {
        return BigDecimal.valueOf(requirement).divide(BigDecimal.valueOf(runesCount), RoundingMode.DOWN).intValue();
    }

    public BigDecimal calculateBonusEffect(Integer baseValue) {
        return effectAggregationType.calculate(baseValue, bonusValue);
    }

    public BigDecimal getEqualizedBonusValue() {
        return BigDecimal.valueOf(requirement == 2 ? getBonusValue() * 2 : getBonusValue());
    }

}
