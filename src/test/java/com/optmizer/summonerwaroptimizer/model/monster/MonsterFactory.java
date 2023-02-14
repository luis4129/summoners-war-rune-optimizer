package com.optmizer.summonerwaroptimizer.model.monster;

import com.optmizer.summonerwaroptimizer.model.build.BuildFactory;

import java.util.List;

public class MonsterFactory {

    public static final Long SWARFARM_ID = 7400575165L;
    public static final Integer LEVEL = 40;
    public static final Integer GRADE = 6;

    public static List<Monster> getValidMonsters() {
        return List.of(getValidMonster());
    }

    public static Monster getValidMonster() {
        return Monster.builder()
            .swarfarmId(SWARFARM_ID)
            .baseMonster(BaseMonsterFactory.getValidBaseMonster())
            .level(LEVEL)
            .grade(GRADE)
            .build(BuildFactory.getValidBuild())
            .build();
    }

}
