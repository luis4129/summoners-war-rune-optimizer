package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.MainStatMaxBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneSlot;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.StatEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MainStatEfficiencyService {

    private final Map<String, Map<Integer, BonusMaxEfficiency>> strategyMaxEfficiencyRatioMap = new HashMap<>();
    private final static Integer SIX_STARS = 6;

    public StatEfficiencyRatio getSubStatEfficiencyRatio(BuildStrategy buildStrategy, Rune rune, List<BonusAttribute> remainingAttributes, BaseMonster baseMonster) {
        var bonusAttribute = rune.getMainStat().getBonusAttribute();
        var maxMainStatEfficiencyRatio = getBestMainStatFromSlot(remainingAttributes, rune.getSlot(), baseMonster);

        if (!buildStrategy.getUsefulAttributesBonus().contains(bonusAttribute))
            return StatEfficiencyRatio.builder()
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(maxMainStatEfficiencyRatio)
                .build();

        var mainStatEfficiencyRatio = getMainStatEfficiencyRatioComparedToSubStat(bonusAttribute, rune.getGrade(), baseMonster);

        return StatEfficiencyRatio.builder()
            .efficiencyRatio(mainStatEfficiencyRatio)
            .maxEfficiencyRatio(maxMainStatEfficiencyRatio)
            .build();
    }

    private BigDecimal getBestMainStatFromSlot(List<BonusAttribute> usefulAttributes, Integer slot, BaseMonster baseMonster) {
        var attributesKey = getAttributesKey(usefulAttributes);
        var slotMaxEfficiencyMap = strategyMaxEfficiencyRatioMap.getOrDefault(attributesKey, new HashMap<>());
        var strategySlotMaxEfficiency = slotMaxEfficiencyMap.get(slot);

        if (Objects.isNull(strategySlotMaxEfficiency)) {
            var maxEfficiency = RuneSlot.values()[slot]
                .getMainStatOptions()
                .stream()
                .filter(usefulAttributes::contains)
                .map(attribute -> getMainStatEfficiencyRatioComparedToSubStat(attribute, baseMonster))
                .max(Comparator.comparing(BonusMaxEfficiency::getRatio))
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());

            slotMaxEfficiencyMap.put(slot, maxEfficiency);
            strategyMaxEfficiencyRatioMap.put(attributesKey, slotMaxEfficiencyMap);
        }

        var bonusMaxEfficiency = strategyMaxEfficiencyRatioMap.get(attributesKey).get(slot);
        usefulAttributes.remove(bonusMaxEfficiency.getAttribute());
        return bonusMaxEfficiency.getRatio();
    }

    private BonusMaxEfficiency getMainStatEfficiencyRatioComparedToSubStat(BonusAttribute attribute, BaseMonster baseMonster) {
        var maxEfficiencyRatio = getMainStatEfficiencyRatioComparedToSubStat(attribute, SIX_STARS, baseMonster);

        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiencyRatio)
            .build();
    }

    private BigDecimal getMainStatEfficiencyRatioComparedToSubStat(BonusAttribute attribute, Integer grade, BaseMonster baseMonster) {
        var mainStatBonus = getMaxGradeBonus(attribute, grade);
        var subStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        return mainStatBonus.divide(subStatBonus, 3, RoundingMode.DOWN);
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

    private BigDecimal getMaxGradeBonus(BonusAttribute bonusAttribute, Integer grade) {
        return MainStatMaxBonus.valueOf(bonusAttribute.name())
            .getMaxGradeBonusList()
            .get(grade);
    }

    private String getAttributesKey(List<BonusAttribute> usefulAttributes) {
        return usefulAttributes
            .stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }
}
