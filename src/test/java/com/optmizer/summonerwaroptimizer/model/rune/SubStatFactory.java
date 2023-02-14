package com.optmizer.summonerwaroptimizer.model.rune;

import java.util.List;

public class SubStatFactory {

    public static final Long ID = 1L;
    public static final BonusAttribute BONUS_ATTRIBUTE = BonusAttribute.SPEED;
    public static final Integer VALUE = 10;
    public static final Integer GRIND_VALUE = 5;
    public static final Boolean IS_ENCHANTED = true;


    public static List<SubStat> getValidSubStats() {
        return List.of(
            getValidSubStat(),
            getValidSubStat(),
            getValidSubStat(),
            getValidSubStat()
        );
    }

    public static SubStat getValidSubStat() {
        return SubStat.builder()
            .id(ID)
            .bonusAttribute(BONUS_ATTRIBUTE)
            .value(VALUE)
            .grindValue(GRIND_VALUE)
            .enchanted(IS_ENCHANTED)
            .build();
    }

    public static List<SubStat> getPrefixStatWithSpeedAttribute() {
        return List.of(SubStat.builder()
            .bonusAttribute(BonusAttribute.SPEED)
            .value(6)
            .grindValue(5)
            .build());
    }

    public static List<SubStat> getPrefixStatWithAttackAttribute() {
        return List.of(SubStat.builder()
            .bonusAttribute(BonusAttribute.ATTACK)
            .value(8)
            .grindValue(10)
            .build());
    }

}
