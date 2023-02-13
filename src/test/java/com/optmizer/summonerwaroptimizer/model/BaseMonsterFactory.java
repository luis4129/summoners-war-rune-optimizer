package com.optmizer.summonerwaroptimizer.model;

import java.util.List;

public class BaseMonsterFactory {

    public static final Long ID = 1L;
    public static final String IMAGE_FILE_NAME = "image_path.png";
    public static final String NAME = "Veromos";
    public static final Integer HIT_POINTS = 10000;
    public static final Integer ATTACK = 500;
    public static final Integer DEFENSE = 500;
    public static final Integer SPEED = 115;
    public static final Integer CRITICAL_RATE = 15;
    public static final Integer CRITICAL_DAMAGE = 50;
    public static final Integer RESISTANCE = 15;
    public static final Integer ACCURACY = 15;

    public static List<BaseMonster> getValidBaseMonsters() {
        return List.of(getValidBaseMonster());
    }

    public static BaseMonster getValidBaseMonster() {
        return BaseMonster.builder()
            .swarfarmId(ID)
            .name(NAME)
            .imageFileName(IMAGE_FILE_NAME)
            .hitPoints(HIT_POINTS)
            .attack(ATTACK)
            .defense(DEFENSE)
            .speed(SPEED)
            .criticalRate(CRITICAL_RATE)
            .criticalDamage(CRITICAL_DAMAGE)
            .resistance(RESISTANCE)
            .accuracy(ACCURACY)
            .build();
    }

}
