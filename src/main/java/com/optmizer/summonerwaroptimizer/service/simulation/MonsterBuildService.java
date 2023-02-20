package com.optmizer.summonerwaroptimizer.service.simulation;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.MainStatBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.PrefixStatBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.RuneSetBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.SubStatBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class MonsterBuildService {

    @Autowired
    private RuneSetBonusService runeSetBonusService;

    @Autowired
    private MainStatBonusService mainStatBonusService;

    @Autowired
    private PrefixStatBonusService prefixStatBonusService;

    @Autowired
    private SubStatBonusService subStatBonusService;

    public MonsterStats getMonsterStats(Monster monster) {
        var baseMonster = monster.getBaseMonster();
        var build = monster.getBuild();

        return getMonsterStats(baseMonster, build);
    }

    public MonsterStats getMonsterStats(BaseMonster baseMonster, Build build) {
        var hitPoints = getAttributeValue(MonsterAttribute.HIT_POINTS, baseMonster.getHitPoints(), build);
        var attack = getAttributeValue(MonsterAttribute.ATTACK, baseMonster.getAttack(), build);
        var defense = getAttributeValue(MonsterAttribute.DEFENSE, baseMonster.getDefense(), build);
        var speed = getAttributeValue(MonsterAttribute.SPEED, baseMonster.getSpeed(), build);
        var criticalRate = getAttributeValue(MonsterAttribute.CRITICAL_RATE, baseMonster.getCriticalRate(), build);
        var criticalDamage = getAttributeValue(MonsterAttribute.CRITICAL_DAMAGE, baseMonster.getCriticalDamage(), build);
        var resistance = getAttributeValue(MonsterAttribute.RESISTANCE, baseMonster.getResistance(), build);
        var accuracy = getAttributeValue(MonsterAttribute.ACCURACY, baseMonster.getAccuracy(), build);

        return MonsterStats.builder()
            .hitPoints(hitPoints)
            .attack(attack)
            .defense(defense)
            .speed(speed)
            .criticalRate(criticalRate)
            .criticalDamage(criticalDamage)
            .resistance(resistance)
            .accuracy(accuracy)
            .build();
    }

    private Integer getAttributeValue(MonsterAttribute monsterAttribute, Integer baseValue, Build build) {
        var runeSetBonus = runeSetBonusService.getRuneSetBonus(monsterAttribute, baseValue, build);
        var mainStatBonus = mainStatBonusService.getMainStatBonus(monsterAttribute, baseValue, build);
        var prefixStatBonus = prefixStatBonusService.getPrefixStatBonus(monsterAttribute, baseValue, build);
        var subStatsBonus = subStatBonusService.getSubStatsBonus(monsterAttribute, baseValue, build);

        return BigDecimal.valueOf(baseValue)
            .add(runeSetBonus)
            .add(mainStatBonus)
            .add(prefixStatBonus)
            .add(subStatsBonus)
            .setScale(0, RoundingMode.UP)
            .intValue();
    }
}
