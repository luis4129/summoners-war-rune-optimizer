package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
public class RuneSetBonusService {

    private final Map<MonsterAttribute, List<RuneSet>> attributeBonusMap = generateAttributeMap();

    public BigDecimal getRuneSetBonus(MonsterAttribute monsterAttribute, Integer baseValue, Build build) {
        var attributeBonusRuneSets = attributeBonusMap.get(monsterAttribute);
        var activeRuneSets = getActiveRuneSets(build.getRunes());

        return attributeBonusRuneSets.stream()
            .filter(activeRuneSets::contains)
            .map(runeSet -> runeSet.calculateBonusEffect(baseValue))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<MonsterAttribute, List<RuneSet>> generateAttributeMap() {
        return Stream.of(RuneSet.values())
            .filter(RuneSet::hasAttributeBonus)
            .collect(groupingBy(RuneSet::getAttribute));

    }


    private List<RuneSet> getActiveRuneSets(List<Rune> equippedRunes) {
        var equippedRuneSetsMap = equippedRunes.stream()
            .collect(Collectors.groupingBy(Rune::getSet))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        return getActiveRuneSetsRecursively(equippedRuneSetsMap, new ArrayList<>());
    }

    private List<RuneSet> getActiveRuneSetsRecursively(Map<RuneSet, Integer> equippedRuneSetsMap, List<RuneSet> activeRuneSets) {
        Optional<RuneSet> equippedRuneSet = equippedRuneSetsMap.keySet().stream().findFirst();

        return equippedRuneSet.map(runeSet -> {
            var unprocessedRuneCount = equippedRuneSetsMap.get(runeSet);
            var runeSetBonusRequirement = runeSet.getRequirement();

            if (unprocessedRuneCount >= runeSetBonusRequirement) {
                activeRuneSets.add(runeSet);
                unprocessedRuneCount -= runeSetBonusRequirement;
                equippedRuneSetsMap.put(runeSet, unprocessedRuneCount);
            } else {
                equippedRuneSetsMap.remove(runeSet);
            }

            return getActiveRuneSetsRecursively(equippedRuneSetsMap, activeRuneSets);

        }).orElse(activeRuneSets);


    }

}
