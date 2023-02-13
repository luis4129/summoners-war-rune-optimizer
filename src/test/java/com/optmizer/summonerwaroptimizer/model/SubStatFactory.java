package com.optmizer.summonerwaroptimizer.model;

import com.optmizer.summonerwaroptimizer.model.rune.Attribute;
import com.optmizer.summonerwaroptimizer.model.rune.SubStat;

import java.util.List;

public class SubStatFactory {

    public static final Long ID = 1L;
    public static final Attribute ATTRIBUTE = Attribute.SPEED;
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
            .attribute(ATTRIBUTE)
            .value(VALUE)
            .grindValue(GRIND_VALUE)
            .enchanted(IS_ENCHANTED)
            .build();
    }

}
