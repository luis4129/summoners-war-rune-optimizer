package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnchantedGemEfficiencyService {

    private final Map<String, BonusMaxEfficiency> strategyMaxEfficiencyRatioMap = new HashMap<>();

    public BigDecimal getBestEnchantedGemEfficiencyRatio(List<BonusAttribute> usefulAttributes, BaseMonster baseMonster) {
        var attributesKey = getAttributesKey(usefulAttributes);
        var strategyMaxEfficiencyRatio = strategyMaxEfficiencyRatioMap.get(attributesKey);

        if (Objects.isNull(strategyMaxEfficiencyRatio)) {
            var maxEfficiency = usefulAttributes
                .stream()
                .map(attribute -> getEnchantedGemEfficiencyRatio(attribute, baseMonster))
                .max(Comparator.comparing(BonusMaxEfficiency::getRatio))
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());


            strategyMaxEfficiencyRatioMap.put(attributesKey, maxEfficiency);
        }

        var bonusMaxEfficiency = strategyMaxEfficiencyRatioMap.get(attributesKey);
        usefulAttributes.remove(bonusMaxEfficiency.getAttribute());
        return bonusMaxEfficiency.getRatio();
    }

    private BonusMaxEfficiency getEnchantedGemEfficiencyRatio(BonusAttribute attribute, BaseMonster baseMonster) {
        var maxEnchantmentBonus = attribute.getMaxEnchantmentSubStatBonus();
        var maxTotalSubStatBonus = getFullyMaxedSubStatBonus(attribute, baseMonster);

        var maxEfficiencyRatio = maxEnchantmentBonus.divide(maxTotalSubStatBonus, 3, RoundingMode.DOWN);
        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiencyRatio)
            .build();
    }

    private String getAttributesKey(List<BonusAttribute> usefulAttributes) {
        return usefulAttributes.stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
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

}
