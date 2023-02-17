package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.StatEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.RuneSetBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RuneSetEfficiencyService {

    @Autowired
    private RuneSetBonusService runeSetBonusService;

    private final Map<String, Map<Integer, BigDecimal>> strategyMaxEfficiencyMap = new HashMap<>();

    public StatEfficiencyRatio getSubStatEfficiencyRatio(BuildStrategy buildStrategy, RuneSet runeSet) {
        if (buildStrategy.hasRequiredRuneSets() || hasUsefulAttributeBonus(buildStrategy, runeSet))
            return StatEfficiencyRatio.builder()
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(BigDecimal.ZERO)
                .build();

        var runeSetEfficiencyRatio = getRuneSetEfficiencyComparedToSubStats(runeSet);
        var maxRuneSetEfficiencyRatio = getBestRuneSetEfficiency(buildStrategy, runeSet.getRequirement());

        return StatEfficiencyRatio.builder()
            .efficiencyRatio(runeSetEfficiencyRatio)
            .maxEfficiencyRatio(maxRuneSetEfficiencyRatio)
            .build();
    }

    private boolean hasUsefulAttributeBonus(BuildStrategy buildStrategy, RuneSet runeSet) {
        return buildStrategy.getUsefulAttributesBonus()
            .stream()
            .map(BonusAttribute::getMonsterAttribute)
            .anyMatch(monsterAttribute -> monsterAttribute.equals(runeSet.getAttribute()));
    }

    private BigDecimal getBestRuneSetEfficiency(BuildStrategy buildStrategy, Integer runeSetRequirement) {
        var attributesKey = getAttributesKey(buildStrategy);
        var runeSetRequirementEfficiencyMap = strategyMaxEfficiencyMap.getOrDefault(attributesKey, new HashMap<>());
        var strategyMaxEfficiency = runeSetRequirementEfficiencyMap.get(runeSetRequirement);

        if (Objects.isNull(strategyMaxEfficiency)) {
            var maxEfficiency = buildStrategy.getUsefulAttributes()
                .stream()
                .map(runeSetBonusService::getRuneSetWhichGiveBonusToAttribute)
                .flatMap(Collection::stream)
                .filter(runeSet -> runeSetRequirement >= runeSet.getRequirement())
                .map(this::getRuneSetEfficiencyComparedToSubStats)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

            runeSetRequirementEfficiencyMap.put(runeSetRequirement, maxEfficiency);
            strategyMaxEfficiencyMap.put(attributesKey, runeSetRequirementEfficiencyMap);
        }

        return strategyMaxEfficiencyMap.get(attributesKey).get(runeSetRequirement);
    }

    private BigDecimal getRuneSetEfficiencyComparedToSubStats(RuneSet runeSet) {
        var maxTotalSubStatBonus = BonusAttribute.valueOf(runeSet.getAttribute().name()).getFullyMaxedSubStatBonus();

        return runeSet.getEqualizedBonusValue().divide(maxTotalSubStatBonus, 3, RoundingMode.DOWN);
    }

    private String getAttributesKey(BuildStrategy buildStrategy) {
        return buildStrategy.getUsefulAttributes()
            .stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }
}
