package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmMonster;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.service.BaseMonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonsterConversionService {

    @Autowired
    private BuildConversionService buildConversionService;

    @Autowired
    private BaseMonsterService baseMonsterService;

    public List<Monster> toMonsters(List<SwarfarmMonster> swarfarmMonsters) {
        return swarfarmMonsters.stream()
            .map(this::toMonster)
            .collect(Collectors.toList());
    }

    public Monster toMonster(SwarfarmMonster swarfarmMonster) {
        var build = buildConversionService.toBuild(swarfarmMonster.getRunes());
        var baseMonster = baseMonsterService.findBySwarfarmId(swarfarmMonster.getMasterId());

        return Monster.builder()
            .swarfarmId(swarfarmMonster.getId())
            .baseMonster(baseMonster)
            .level(swarfarmMonster.getLevel())
            .grade(swarfarmMonster.getGrade())
            .build(build)
            .build();

    }
}
