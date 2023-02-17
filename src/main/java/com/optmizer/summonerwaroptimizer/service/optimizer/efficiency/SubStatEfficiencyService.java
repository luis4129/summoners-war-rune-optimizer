package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneSlot;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.StatEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.SubStatBonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.SubStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SubStatEfficiencyService {

    @Autowired
    private EnchantedGemEfficiencyService enchantedGemEfficiencyService;

    private final Map<String, BonusMaxEfficiency> strategyMaxEfficiencyRatioMap = new HashMap<>();

    public StatEfficiencyRatio getSubStatEfficiencyRatio(BuildStrategy buildStrategy, Rune rune, List<BonusAttribute> usefulAttributes, BaseMonster baseMonster) {
        var maxSubStatsEfficiencyRatio = getMaxEfficiencyRatio(usefulAttributes, rune.getSlot(), baseMonster);
        var subStatsEfficiencyRatio = getSubStatEfficiencyRatio(buildStrategy, rune.getSubStats(), baseMonster);

        return StatEfficiencyRatio.builder()
            .efficiencyRatio(subStatsEfficiencyRatio)
            .maxEfficiencyRatio(maxSubStatsEfficiencyRatio)
            .build();
    }


    private BigDecimal getSubStatEfficiencyRatio(BuildStrategy buildStrategy, List<SubStat> subStats, BaseMonster baseMonster) {
        return subStats
            .stream()
            .filter(subStat -> buildStrategy.getUsefulAttributesBonus().contains(subStat.getBonusAttribute()))
            .map(attribute -> getSubStatEfficiencyRatio(attribute, baseMonster))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getSubStatEfficiencyRatio(SubStat subStat, BaseMonster baseMonster) {
        var fullyMaxedSubStatBonus = getFullyMaxedSubStatBonus(subStat.getBonusAttribute(), baseMonster);

        return subStat.getFullyGrindedValue().divide(fullyMaxedSubStatBonus, 3, RoundingMode.DOWN);
    }


    private BigDecimal getMaxEfficiencyRatio(List<BonusAttribute> usefulAttributes, Integer runeSlot, BaseMonster baseMonster) {
        removeInvalidAttributeOptions(usefulAttributes, runeSlot);

        var maxSubStatEfficiencyRatio = getBestSubStatEfficiencyRatio(usefulAttributes, baseMonster);
        var maxEnchantedSubStatEfficiencyRatio = enchantedGemEfficiencyService.getBestEnchantedGemEfficiencyRatio(usefulAttributes, baseMonster);

        return usefulAttributes.stream()
            .map(bonusAttribute -> getSubStatMaxEfficiencyRatio(bonusAttribute, baseMonster))
            .sorted(Comparator.comparing(BonusMaxEfficiency::getRatio))
            .limit(2)
            .map(bonusMaxEfficiency -> {
                usefulAttributes.remove(bonusMaxEfficiency.getAttribute());
                return bonusMaxEfficiency.getRatio();
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .add(maxSubStatEfficiencyRatio)
            .add(maxEnchantedSubStatEfficiencyRatio);
    }

    private BigDecimal getBestSubStatEfficiencyRatio(List<BonusAttribute> usefulAttributes, BaseMonster baseMonster) {
        var attributesKey = getAttributesKey(usefulAttributes);
        var strategyMaxEfficiency = strategyMaxEfficiencyRatioMap.get(attributesKey);

        if (Objects.isNull(strategyMaxEfficiency)) {
            var maxEfficiency = usefulAttributes
                .stream()
                .map(attribute -> getBestSubStatMaxEfficiencyRatio(attribute, baseMonster))
                .max(Comparator.comparing(SubStatBonusMaxEfficiency::getRatio))
                .map(SubStatBonusMaxEfficiency::toBonusMaxEfficiency)
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());

            strategyMaxEfficiencyRatioMap.put(attributesKey, maxEfficiency);
        }

        var bonusMaxEfficiency = strategyMaxEfficiencyRatioMap.get(attributesKey);
        usefulAttributes.remove(bonusMaxEfficiency.getAttribute());
        return bonusMaxEfficiency.getRatio();
    }

    private void removeInvalidAttributeOptions(List<BonusAttribute> usefulAttributes, Integer runeSlot) {
        RuneSlot.values()[runeSlot]
            .getInvalidSubStatOptions()
            .stream()
            .filter(usefulAttributes::contains)
            .forEach(usefulAttributes::remove);
    }

    private BonusMaxEfficiency getSubStatMaxEfficiencyRatio(BonusAttribute attribute, BaseMonster baseMonster) {
        var maxGrindedSubStatBonus = attribute.getMaxGrindedSubStatBonus();
        var bestTotalSubStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        var maxEfficiency = maxGrindedSubStatBonus.divide(bestTotalSubStatBonus, 3, RoundingMode.DOWN);

        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiency)
            .build();
    }

    private SubStatBonusMaxEfficiency getBestSubStatMaxEfficiencyRatio(BonusAttribute attribute, BaseMonster baseMonster) {
        var maxGrindedSubStatBonus = attribute.getMaxGrindedSubStatBonus();
        var fullyMaxedSubStatBonus = attribute.getFullyMaxedSubStatBonus();
        var bestTotalSubStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        var maxEfficiency = maxGrindedSubStatBonus.divide(bestTotalSubStatBonus, 3, RoundingMode.DOWN);
        var fullMaxEfficiency = fullyMaxedSubStatBonus.divide(bestTotalSubStatBonus, 3, RoundingMode.DOWN);

        return SubStatBonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiency)
            .fullRatio(fullMaxEfficiency)
            .build();
    }

    private BigDecimal getFullyMaxedSubStatBonus(BonusAttribute bonusAttribute, BaseMonster baseMonster) {
        return switch (bonusAttribute) {
            case FLAT_HIT_POINTS ->
                getFullyMaxedSubStatBonusFromPercentageAttribute(BonusAttribute.HIT_POINTS, baseMonster.getHitPoints());
            case FLAT_ATTACK ->
                getFullyMaxedSubStatBonusFromPercentageAttribute(BonusAttribute.ATTACK, baseMonster.getAttack());
            case FLAT_DEFENSE ->
                getFullyMaxedSubStatBonusFromPercentageAttribute(BonusAttribute.DEFENSE, baseMonster.getDefense());
            default -> bonusAttribute.getFullyMaxedSubStatBonus();
        };
    }

    private BigDecimal getFullyMaxedSubStatBonusFromPercentageAttribute(BonusAttribute percentageAttribute, Integer baseAttributeValue) {
        var maxMultiplierBonus = percentageAttribute.getFullyMaxedSubStatBonus();
        return percentageAttribute.getEffectAggregationType().calculate(baseAttributeValue, maxMultiplierBonus.intValue());
    }

    private String getAttributesKey(List<BonusAttribute> usefulAttributes) {
        return usefulAttributes
            .stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }

}
