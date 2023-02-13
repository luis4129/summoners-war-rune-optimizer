package com.optmizer.summonerwaroptimizer.model;

import com.optmizer.summonerwaroptimizer.model.rune.Attribute;
import com.optmizer.summonerwaroptimizer.model.rune.PrefixStat;

public class PrefixStatFactory {

    public static final Attribute ATTRIBUTE = Attribute.SPEED;
    public static final Integer VALUE = 42;


    public static PrefixStat getValidPrefixStat() {
        return PrefixStat.builder()
            .attribute(ATTRIBUTE)
            .value(VALUE)
            .build();
    }

}
