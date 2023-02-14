package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;

@Service
public class SubStatBonusService {

    public BigDecimal getSubStatsBonus(MonsterAttribute monsterAttribute, Integer baseMonsterAttributeValue, Build build) {
        return build.getRunes()
            .stream()
            .map(Rune::getSubStats)
            .flatMap(Collection::stream)
            .filter(subStat -> subStat.getBonusAttribute().getMonsterAttribute().equals(monsterAttribute))
            .map(subStat -> subStat.getBonusAttributeValue(baseMonsterAttributeValue))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
