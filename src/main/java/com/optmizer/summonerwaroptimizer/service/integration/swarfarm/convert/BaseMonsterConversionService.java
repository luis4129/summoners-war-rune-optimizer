package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmBaseMonster;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BaseMonsterConversionService {

    public List<BaseMonster> toBaseMonsters(List<SwarfarmBaseMonster> swarfarmBaseMonsters) {
        return swarfarmBaseMonsters.stream()
            .map(this::toBaseMonster)
            .collect(Collectors.toList());
    }

    public BaseMonster toBaseMonster(SwarfarmBaseMonster swarfarmBaseMonster) {
        return BaseMonster.builder()
            .swarfarmId(swarfarmBaseMonster.getId())
            .name(swarfarmBaseMonster.getName())
            .imageFileName(swarfarmBaseMonster.getImageFileName())
            .hitPoints(swarfarmBaseMonster.getHitPoints())
            .attack(swarfarmBaseMonster.getAttack())
            .defense(swarfarmBaseMonster.getDefense())
            .speed(swarfarmBaseMonster.getSpeed())
            .criticalRate(swarfarmBaseMonster.getCriticalRate())
            .criticalDamage(swarfarmBaseMonster.getCriticalDamage())
            .resistance(swarfarmBaseMonster.getResistance())
            .accuracy(swarfarmBaseMonster.getAccuracy())
            .build();
    }
}
