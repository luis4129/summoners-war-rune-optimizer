package com.optmizer.summonerwaroptimizer.model;

import java.util.List;

public class MonsterFactory {

    public static final Long ID = 1L;
    public static final Integer LEVEL = 40;
    public static final Integer GRADE = 6;

    public static List<Monster> getValidMonsters() {
        return List.of(getValidMonster());
    }

    public static Monster getValidMonster() {
        return Monster.builder()
            .id(ID)
            .baseMonster(BaseMonsterFactory.getValidBaseMonster())
            .level(LEVEL)
            .grade(GRADE)
            .runes(RuneFactory.getValidRunes())
            .build();
    }

}
