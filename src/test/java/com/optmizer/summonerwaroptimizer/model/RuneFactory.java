package com.optmizer.summonerwaroptimizer.model;

import java.util.List;

public class RuneFactory {

    public static final RuneSet SET = RuneSet.VIOLENT;
    public static final Integer SLOT = 2;
    public static final Integer GRADE = 6;
    public static final Integer LEVEL = 15;


    public static List<Rune> getValidRunes() {
        return List.of(
            getValidRune(),
            getValidRune(),
            getValidRune(),
            getValidRune(),
            getValidRune(),
            getValidRune()
        );
    }

    public static Rune getValidRune() {
        return Rune.builder()
            .set(SET)
            .slot(SLOT)
            .grade(GRADE)
            .level(LEVEL)
            .mainStat(Attribute.SPEED)
            .prefixStat(PrefixStatFactory.getValidPrefixStat())
            .subStats(SubStatFactory.getValidSubStats())
            .build();
    }

}
