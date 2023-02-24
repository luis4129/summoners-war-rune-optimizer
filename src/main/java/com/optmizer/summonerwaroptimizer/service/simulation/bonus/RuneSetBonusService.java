package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class RuneSetBonusService {

    private final Map<MonsterAttribute, List<RuneSet>> attributeBonusMap = generateAttributeMap();

    public BigDecimal getRuneSetBonus(MonsterAttribute monsterAttribute, Integer baseValue, Build build) {
        var runeSetsThatGiveBonusToAttribute = getRuneSetWhichGiveBonusToAttribute(monsterAttribute);
        var activeRuneSetEffectsMultiplierMap = getEquippedRuneSetsCountMap(build.getRunes());

        return runeSetsThatGiveBonusToAttribute.stream()
            .filter(activeRuneSetEffectsMultiplierMap::containsKey)
            .map(runeSet -> runeSet.calculateBonusEffect(baseValue).multiply(activeRuneSetEffectsMultiplierMap.get(runeSet)))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<RuneSet> getRuneSetWhichGiveBonusToAttribute(MonsterAttribute monsterAttribute) {
        return attributeBonusMap.get(monsterAttribute);
    }

    private Map<MonsterAttribute, List<RuneSet>> generateAttributeMap() {
        return Stream.of(RuneSet.values())
            .filter(RuneSet::hasAttributeBonus)
            .collect(groupingBy(RuneSet::getAttribute));

    }


    public Map<RuneSet, BigDecimal> getEquippedRuneSetsCountMap(List<Rune> equippedRunes) {
        return equippedRunes.stream()
            .collect(Collectors.groupingBy(Rune::getSet))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                var runesCount = BigDecimal.valueOf(entry.getValue().size());
                var runeRequirement = BigDecimal.valueOf(entry.getKey().getRequirement());
                return runesCount.divide(runeRequirement, 0, RoundingMode.DOWN);
            }));
    }

}
