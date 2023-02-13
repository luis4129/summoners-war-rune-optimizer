package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import java.util.List;

public class SwarfarmMonsterFactory {

    public static final Long ID = 1L;
    public static final Long MASTER_ID = 1L;
    public static final Integer LEVEL = 40;
    public static final Integer GRADE = 6;

    public static List<SwarfarmMonster> getValidSwarfarmMonsters() {
        return List.of(getValidSwarfarmMonster());
    }

    public static SwarfarmMonster getValidSwarfarmMonster() {
        return SwarfarmMonster.builder()
            .id(ID)
            .masterId(MASTER_ID)
            .level(LEVEL)
            .grade(GRADE)
            .runes(SwarfarmRuneFactory.getValidSwarfarmRunes())
            .build();
    }

}
