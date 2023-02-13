package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import java.util.List;

public class SwarfarmRuneFactory {

    public static final Integer SET = 13;
    public static final Integer SLOT = 2;
    public static final Integer GRADE = 6;
    public static final Integer LEVEL = 15;
    public static final Integer ATTRIBUTE = 8;
    public static final Integer VALUE = 42;
    public static final Integer GRIND_VALUE = 5;
    public static final Integer ENCHANTED_VALUE = 1;
    public static final Integer EMPTY = 0;

    public static List<SwarfarmRune> getValidSwarfarmRunes() {
        return List.of(
            getValidSwarfarmRune(),
            getValidSwarfarmRune(),
            getValidSwarfarmRune(),
            getValidSwarfarmRune(),
            getValidSwarfarmRune(),
            getValidSwarfarmRune()
        );
    }

    public static SwarfarmRune getValidSwarfarmRune() {
        return SwarfarmRune.builder()
            .set(SET)
            .slot(SLOT)
            .grade(GRADE)
            .level(LEVEL)
            .primaryEffect(getValidSwarfarmAttribute())
            .prefixEffect(getValidSwarfarmAttribute())
            .secondaryEffects(getValidSwarfarmSubStats())
            .build();
    }

    public static List<Integer> getValidSwarfarmAttribute() {
        return List.of(ATTRIBUTE, VALUE);
    }

    public static List<Integer> getEmptySwarfarmAttribute() {
        return List.of(EMPTY, EMPTY);
    }

    public static List<List<Integer>> getValidSwarfarmSubStats() {
        return List.of(
            List.of(ATTRIBUTE, VALUE, GRIND_VALUE, ENCHANTED_VALUE),
            List.of(ATTRIBUTE, VALUE, GRIND_VALUE, ENCHANTED_VALUE),
            List.of(ATTRIBUTE, VALUE, GRIND_VALUE, ENCHANTED_VALUE),
            List.of(ATTRIBUTE, VALUE, GRIND_VALUE, ENCHANTED_VALUE)
        );
    }

    public static List<List<Integer>> getEmptySwarfarmSubStats() {
        return List.of(
            List.of(EMPTY, EMPTY, EMPTY, EMPTY),
            List.of(EMPTY, EMPTY, EMPTY, EMPTY),
            List.of(EMPTY, EMPTY, EMPTY, EMPTY),
            List.of(EMPTY, EMPTY, EMPTY, EMPTY)
        );
    }
}
