package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class PrefixStatBonusService {

    public BigDecimal getPrefixStatBonus(MonsterAttribute monsterAttribute, Integer baseMonsterAttributeValue, Build build) {
        return build.getRunes()
            .stream()
            .map(Rune::getPrefixStat)
            .filter(Objects::nonNull)
            .filter(prefixStat -> prefixStat.getBonusAttribute().getMonsterAttribute().equals(monsterAttribute))
            .map(prefixStat -> prefixStat.getBonusAttributeValue(baseMonsterAttributeValue))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
