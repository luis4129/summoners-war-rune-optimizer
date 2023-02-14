package com.optmizer.summonerwaroptimizer.model.monster;

import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;

public class MonsterStatsFactory {
    public static final Integer HIT_POINTS = 10000;
    public static final Integer ATTACK = 500;
    public static final Integer DEFENSE = 500;
    public static final Integer SPEED = 115;
    public static final Integer CRITICAL_RATE = 15;
    public static final Integer CRITICAL_DAMAGE = 50;
    public static final Integer RESISTANCE = 15;
    public static final Integer ACCURACY = 15;

    public static MonsterStats getValidMonsterStats() {
        return MonsterStats.builder()
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
