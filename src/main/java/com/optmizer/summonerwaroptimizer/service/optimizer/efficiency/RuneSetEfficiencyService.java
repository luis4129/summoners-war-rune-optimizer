package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.PriorityEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiencyRatio;
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

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
public class RuneSetEfficiencyService {

    @Autowired
    private RuneSetBonusService runeSetBonusService;

    private final Map<String, Map<Integer, BigDecimal>> strategyMaxEfficiencyMap = new HashMap<>();

    public RuneEfficiencyRatio getSubStatEfficiencyRatio(BaseMonster baseMonster, RuneSet runeSet,
                                                         Map<Integer, List<MonsterAttribute>> usefulAttributesByPriorityMap,
                                                         List<MonsterAttribute> limitedAttributes,
                                                         List<RuneSet> requiredRuneSets) {
        var limitedAttributeBonuses = getLimitedAttributeBonuses(limitedAttributes, runeSet, baseMonster);
        var priorityEfficiencyRatios = usefulAttributesByPriorityMap.keySet()
            .stream()
            .map(priority -> getSubStatPriorityEfficiencyRatio(usefulAttributesByPriorityMap, priority, runeSet, requiredRuneSets))
            .toList();

        return RuneEfficiencyRatio.builder()
            .limitedAttributeBonuses(limitedAttributeBonuses)
            .priorityEfficiencyRatios(priorityEfficiencyRatios)
            .build();
    }

    public PriorityEfficiencyRatio getSubStatPriorityEfficiencyRatio(Map<Integer, List<MonsterAttribute>> usefulAttributesByPriorityMap, Integer priority, RuneSet runeSet, List<RuneSet> requiredRuneSets) {
        var usefulAttributes = usefulAttributesByPriorityMap.get(priority);

        if (!isEmpty(requiredRuneSets) && areRequiredRuneSetUseless(usefulAttributes, requiredRuneSets))
            return PriorityEfficiencyRatio.builder()
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(BigDecimal.ZERO)
                .build();

        var maxRuneSetEfficiencyRatio = getBestRuneSetEfficiency(usefulAttributes, runeSet.getRequirement());

        if (isRuneSetBonusUseless(usefulAttributes, runeSet))
            return PriorityEfficiencyRatio.builder()
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(maxRuneSetEfficiencyRatio)
                .build();

        var runeSetEfficiencyRatio = getRuneSetEfficiencyComparedToSubStats(runeSet);

        return PriorityEfficiencyRatio.builder()
            .priority(priority)
            .efficiencyRatio(runeSetEfficiencyRatio)
            .maxEfficiencyRatio(maxRuneSetEfficiencyRatio)
            .build();
    }

    private boolean areRequiredRuneSetUseless(List<MonsterAttribute> usefulAttributesBonus, List<RuneSet> requiredRuneSets) {
        if (requiredRuneSets.contains(RuneSet.ANY))
            return false;

        return requiredRuneSets.stream()
            .map(RuneSet::getAttribute)
            .anyMatch(usefulAttributesBonus::contains);
    }

    private boolean isRuneSetBonusUseless(List<MonsterAttribute> usefulAttributesBonus, RuneSet runeSet) {
        return !usefulAttributesBonus.contains(runeSet.getAttribute());
    }

    private BigDecimal getBestRuneSetEfficiency(List<MonsterAttribute> usefulAttributes, Integer runeSetRequirement) {
        var attributesKey = getAttributesKey(usefulAttributes);
        var runeSetRequirementEfficiencyMap = strategyMaxEfficiencyMap.getOrDefault(attributesKey, new HashMap<>());
        var strategyMaxEfficiency = runeSetRequirementEfficiencyMap.get(runeSetRequirement);

        if (Objects.isNull(strategyMaxEfficiency)) {
            var maxEfficiency = usefulAttributes
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
        var bonusValue = runeSet.getEqualizedBonusValue();
        var maxTotalSubStatBonus = BonusAttribute.valueOf(runeSet.getAttribute().name()).getFullyMaxedSubStatBonus();

        return bonusValue.divide(maxTotalSubStatBonus, 4, RoundingMode.DOWN);
    }

    private List<LimitedAttributeBonus> getLimitedAttributeBonuses(List<MonsterAttribute> limitedAttributes, RuneSet runeSet, BaseMonster baseMonster) {
        var monsterAttribute = runeSet.getAttribute();

        if (!limitedAttributes.contains(monsterAttribute))
            return Collections.emptyList();

        var bonusValue = runeSet.getEqualizedBonusValue();
        var flatAttributeBonus = getFlatRuneSetBonus(runeSet, baseMonster, bonusValue);

        var attributeBonus = LimitedAttributeBonus.builder()
            .monsterAttribute(monsterAttribute)
            .bonus(flatAttributeBonus)
            .build();

        return List.of(attributeBonus);
    }

    private BigDecimal getFlatRuneSetBonus(RuneSet runeSet, BaseMonster baseMonster, BigDecimal bonusValue) {
        return switch (runeSet) {
            case ENERGY, FATAL, SWIFT, GUARD, FIGHT, DETERMINATION, ENHANCE -> {
                var monsterAttribute = runeSet.getAttribute();
                var baseValue = baseMonster.getAttributeValue(monsterAttribute);

                yield runeSet.getEffectAggregationType().calculate(baseValue, bonusValue.intValue());
            }
            default -> runeSet.getEqualizedBonusValue();
        };
    }

    private String getAttributesKey(List<MonsterAttribute> attributes) {
        return attributes
            .stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }
}
