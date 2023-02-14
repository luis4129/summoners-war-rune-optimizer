package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class MainStatBonusService {

    public BigDecimal getMainStatBonus(MonsterAttribute monsterAttribute, Integer baseMonsterAttributeValue, Build build) {
        return build.getRunes()
            .stream()
            .map(Rune::getMainStat)
            .filter(mainStat -> mainStat.getBonusAttribute().getMonsterAttribute().equals(monsterAttribute))
            .map(mainStat -> mainStat.getBonusAttributeValue(baseMonsterAttributeValue))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
