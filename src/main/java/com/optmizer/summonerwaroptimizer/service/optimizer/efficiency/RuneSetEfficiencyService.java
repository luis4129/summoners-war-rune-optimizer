package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.RuneSetBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
public class RuneSetEfficiencyService {

    @Autowired
    private RuneSetBonusService runeSetBonusService;

    private final Map<String, Map<Integer, BigDecimal>> strategyMaxEfficiencyMap = new HashMap<>();

    public BigDecimal getRuneSetEfficiencyRatio(RuneSet runeSet, List<RuneSet> requiredRuneSets, List<MonsterAttribute> usefulAttributes) {
        if (notWithinRequiredSets(runeSet, requiredRuneSets) || isRuneSetBonusUseless(usefulAttributes, runeSet))
            return BigDecimal.ZERO;

        var bonusValue = runeSet.getEqualizedBonusValue();
        var maxTotalSubStatBonus = BonusAttribute.valueOf(runeSet.getAttribute().name()).getFullyMaxedSubStatBonus();

        return bonusValue.divide(maxTotalSubStatBonus, 4, RoundingMode.DOWN);
    }

    public BigDecimal getMaxRuneSetEfficiency(Integer runeSetRequirement, List<RuneSet> requiredRuneSets, List<MonsterAttribute> usefulAttributes) {
        if (!isEmpty(requiredRuneSets) && areRequiredRuneSetUseless(usefulAttributes, requiredRuneSets))
            return BigDecimal.ZERO;

        var attributesKey = getAttributesKey(usefulAttributes);
        var runeSetRequirementEfficiencyMap = strategyMaxEfficiencyMap.getOrDefault(attributesKey, new HashMap<>());
        var strategyMaxEfficiency = runeSetRequirementEfficiencyMap.get(runeSetRequirement);

        if (Objects.isNull(strategyMaxEfficiency)) {
            var maxEfficiency = usefulAttributes
                .stream()
                .map(runeSetBonusService::getRuneSetWhichGiveBonusToAttribute)
                .flatMap(Collection::stream)
                .filter(runeSet -> runeSetRequirement >= runeSet.getRequirement())
                .map(runeSet -> getRuneSetEfficiencyRatio(runeSet, requiredRuneSets))
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

            runeSetRequirementEfficiencyMap.put(runeSetRequirement, maxEfficiency);
            strategyMaxEfficiencyMap.put(attributesKey, runeSetRequirementEfficiencyMap);
        }

        return strategyMaxEfficiencyMap.get(attributesKey).get(runeSetRequirement);
    }

    public List<LimitedAttributeBonus> getLimitedAttributeBonuses(BaseMonster baseMonster, RuneSet runeSet, List<MonsterAttribute> limitedAttributes) {
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

    private BigDecimal getRuneSetEfficiencyRatio(RuneSet runeSet, List<RuneSet> requiredRuneSets) {
        if (notWithinRequiredSets(runeSet, requiredRuneSets))
            return BigDecimal.ZERO;

        var bonusValue = runeSet.getEqualizedBonusValue();
        var maxTotalSubStatBonus = BonusAttribute.valueOf(runeSet.getAttribute().name()).getFullyMaxedSubStatBonus();

        return bonusValue.divide(maxTotalSubStatBonus, 4, RoundingMode.DOWN);
    }

    private boolean notWithinRequiredSets(RuneSet runeSet, List<RuneSet> requiredRuneSets) {
        return !ObjectUtils.isEmpty(requiredRuneSets) && !requiredRuneSets.stream().filter(requiredSet -> !requiredSet.equals(RuneSet.ANY)).toList().contains(runeSet);
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
