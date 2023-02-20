package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.MainStatMaxBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneSlot;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
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

    public BigDecimal getMainStatEfficiencyRatio(BaseMonster baseMonster, BonusAttribute attribute, Integer grade, List<BonusAttribute> usefulAttributes) {
        if (!usefulAttributes.contains(attribute))
            return BigDecimal.ZERO;

        var mainStatBonus = getFullyLeveledGradeBonus(attribute, grade);
        var subStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        return mainStatBonus.divide(subStatBonus, 4, RoundingMode.DOWN);
    }

    public BigDecimal getMaxMainStatFromSlot(BaseMonster baseMonster, Integer slot, List<BonusAttribute> remainingAttributes) {
        var attributesKey = getAttributesKey(remainingAttributes);
        var slotMaxEfficiencyMap = strategyMaxEfficiencyRatioMap.getOrDefault(attributesKey, new HashMap<>());
        var strategySlotMaxEfficiency = slotMaxEfficiencyMap.get(slot);

        if (Objects.isNull(strategySlotMaxEfficiency)) {
            var maxEfficiency = RuneSlot.values()[slot]
                .getMainStatOptions()
                .stream()
                .filter(remainingAttributes::contains)
                .map(attribute -> getMainStatEfficiencyRatio(attribute, baseMonster))
                .max(Comparator.comparing(BonusMaxEfficiency::getRatio))
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());

            slotMaxEfficiencyMap.put(slot, maxEfficiency);
            strategyMaxEfficiencyRatioMap.put(attributesKey, slotMaxEfficiencyMap);
        }

        var bonusMaxEfficiency = strategyMaxEfficiencyRatioMap.get(attributesKey).get(slot);
        remainingAttributes.remove(bonusMaxEfficiency.getAttribute());
        return bonusMaxEfficiency.getRatio();
    }

    public List<LimitedAttributeBonus> getLimitedAttributeBonuses(BaseMonster baseMonster, BonusAttribute attribute, Integer grade, List<MonsterAttribute> limitedAttributes) {
        var monsterAttribute = attribute.getMonsterAttribute();

        if (!limitedAttributes.contains(monsterAttribute))
            return Collections.emptyList();

        var mainStatBonus = getFullyLeveledGradeBonus(attribute, grade);
        var flatBonus = getFlatAttributeBonus(attribute, baseMonster, mainStatBonus);

        var attributeBonus = LimitedAttributeBonus.builder()
            .monsterAttribute(monsterAttribute)
            .bonus(flatBonus)
            .build();

        return List.of(attributeBonus);
    }

    private BonusMaxEfficiency getMainStatEfficiencyRatio(BonusAttribute attribute, BaseMonster baseMonster) {
        var mainStatBonus = getFullyLeveledGradeBonus(attribute, SIX_STARS);
        var subStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        var maxEfficiencyRatio = mainStatBonus.divide(subStatBonus, 4, RoundingMode.DOWN);

        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiencyRatio)
            .build();
    }

    private BigDecimal getFlatAttributeBonus(BonusAttribute bonusAttribute, BaseMonster baseMonster, BigDecimal mainStatBonus) {
        return switch (bonusAttribute) {
            case HIT_POINTS, ATTACK, DEFENSE -> {
                var monsterAttribute = bonusAttribute.getMonsterAttribute();
                var baseAttributeValue = baseMonster.getAttributeValue(monsterAttribute);

                yield bonusAttribute.getEffectAggregationType().calculate(mainStatBonus.intValue(), baseAttributeValue);
            }
            default -> mainStatBonus;
        };
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

    private BigDecimal getFullyLeveledGradeBonus(BonusAttribute bonusAttribute, Integer grade) {
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
