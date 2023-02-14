package com.optmizer.summonerwaroptimizer.model.rune;

public class MainStatFactory {

    public static final BonusAttribute BONUS_ATTRIBUTE = BonusAttribute.SPEED;
    public static final Integer VALUE = 42;

    public static MainStat getValidMainStat() {
        return MainStat.builder()
            .bonusAttribute(BONUS_ATTRIBUTE)
            .value(VALUE)
            .build();
    }

    public static MainStat getMainStatWithSpeedAttribute() {
        return MainStat.builder()
            .bonusAttribute(BonusAttribute.SPEED)
            .value(42)
            .build();
    }

    public static MainStat getMainStatWithAttackAttribute() {
        return MainStat.builder()
            .bonusAttribute(BonusAttribute.ATTACK)
            .value(63)
            .build();
    }
}
