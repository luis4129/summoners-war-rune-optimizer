package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import org.assertj.core.util.Arrays;

import java.util.List;

public class SwarfarmBaseMonsterFactory {

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

    public static List<SwarfarmBaseMonster> getValidBaseMonsters() {
        return List.of(getValidBaseMonster());
    }

    public static SwarfarmBaseMonster[] getValidBaseMonstersArray() {
        return Arrays.array(getValidBaseMonster());
    }

    public static SwarfarmBaseMonster getValidBaseMonster() {
        return SwarfarmBaseMonster.builder()
            .id(ID)
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
