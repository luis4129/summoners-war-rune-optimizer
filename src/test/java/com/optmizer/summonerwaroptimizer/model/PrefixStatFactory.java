package com.optmizer.summonerwaroptimizer.model;

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
