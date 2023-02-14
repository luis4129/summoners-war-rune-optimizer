package com.optmizer.summonerwaroptimizer.model.rune;

public class PrefixStatFactory {

    public static final BonusAttribute BONUS_ATTRIBUTE = BonusAttribute.SPEED;
    public static final Integer VALUE = 5;

    public static PrefixStat getValidPrefixStat() {
        return PrefixStat.builder()
            .bonusAttribute(BONUS_ATTRIBUTE)
            .value(VALUE)
            .build();
    }

    public static PrefixStat getPrefixStatWithSpeedAttribute() {
        return PrefixStat.builder()
            .bonusAttribute(BonusAttribute.SPEED)
            .value(6)
            .build();
    }

    public static PrefixStat getPrefixStatWithAttackAttribute() {
        return PrefixStat.builder()
            .bonusAttribute(BonusAttribute.ATTACK)
            .value(8)
            .build();
    }
}
