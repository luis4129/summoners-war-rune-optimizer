package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.PriorityEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiencyRatio;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.PrefixStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrefixStatEfficiencyService {

    private final Map<String, BigDecimal> strategyMaxEfficiencyRatioMap = new HashMap<>();

    public RuneEfficiencyRatio getSubStatEfficiencyRatioValue(BaseMonster baseMonster, PrefixStat prefixStat,
                                                              Map<Integer, List<BonusAttribute>> usefulAttributesByPriorityMap,
                                                              List<MonsterAttribute> limitedAttributes,
                                                              Map<Integer, List<BonusAttribute>> remainingAttributesByPriorityMap) {
        var limitedAttributeBonuses = getLimitedAttributeBonuses(prefixStat, baseMonster, limitedAttributes);
        var priorityEfficiencyRatios = remainingAttributesByPriorityMap.entrySet()
            .stream()
            .map(entry -> getSubStatPriorityEfficiencyRatioValue(usefulAttributesByPriorityMap, entry.getKey(), prefixStat, entry.getValue()))
            .toList();

        return RuneEfficiencyRatio.builder()
            .limitedAttributeBonuses(limitedAttributeBonuses)
            .priorityEfficiencyRatios(priorityEfficiencyRatios)
            .build();
    }

    public PriorityEfficiencyRatio getSubStatPriorityEfficiencyRatioValue(Map<Integer, List<BonusAttribute>> usefulAttributesByPriorityMap, Integer priority, PrefixStat prefixStat, List<BonusAttribute> remainingAttributes) {
        var maxEfficiencyRatio = getBestPrefixStatEfficiencyRatio(remainingAttributes);

        if (Objects.isNull(prefixStat) || !usefulAttributesByPriorityMap.get(priority).contains(prefixStat.getBonusAttribute())) {
            return PriorityEfficiencyRatio.builder()
                .priority(priority)
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(maxEfficiencyRatio)
                .build();
        }

        var efficiencyRatio = getPrefixStatEfficiencyRatio(prefixStat);

        return PriorityEfficiencyRatio.builder()
            .priority(priority)
            .efficiencyRatio(efficiencyRatio)
            .maxEfficiencyRatio(maxEfficiencyRatio)
            .build();
    }

    public BigDecimal getBestPrefixStatEfficiencyRatio(List<BonusAttribute> usefulAttributes) {
        var attributesKey = getAttributesKey(usefulAttributes);
        var strategyMaxEfficiencyRatio = strategyMaxEfficiencyRatioMap.get(attributesKey);

        if (Objects.isNull(strategyMaxEfficiencyRatio)) {
            var maxEfficiency = usefulAttributes
                .stream()
                .map(this::getPrefixStatEfficiencyRatio)
                .max(Comparator.comparing(BonusMaxEfficiency::getRatio))
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());

            usefulAttributes.remove(maxEfficiency.getAttribute());
            strategyMaxEfficiencyRatioMap.put(attributesKey, maxEfficiency.getRatio());
        }

        return strategyMaxEfficiencyRatioMap.get(attributesKey);
    }

    private BonusMaxEfficiency getPrefixStatEfficiencyRatio(BonusAttribute attribute) {
        var maxTotalSubStatBonus = attribute.getMaxAncientStart();
        var fullyMaxedSubStatBonus = attribute.getFullyMaxedSubStatBonus();

        var maxEfficiencyRatio = maxTotalSubStatBonus.divide(fullyMaxedSubStatBonus, 4, RoundingMode.DOWN);
        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiencyRatio)
            .build();
    }

    private BigDecimal getPrefixStatEfficiencyRatio(PrefixStat prefixStat) {
        var bonusAttribute = prefixStat.getBonusAttribute();

        var value = prefixStat.getValue();
        var fullyMaxedSubStatBonus = bonusAttribute.getFullyMaxedSubStatBonus();

        return BigDecimal.valueOf(value).divide(fullyMaxedSubStatBonus, 4, RoundingMode.DOWN);
    }

    private List<LimitedAttributeBonus> getLimitedAttributeBonuses(PrefixStat prefixStat, BaseMonster baseMonster, List<MonsterAttribute> limitedAttributes) {
        if (prefixStat == null)
            return Collections.emptyList();

        var bonusAttribute = prefixStat.getBonusAttribute();
        var monsterAttribute = bonusAttribute.getMonsterAttribute();

        if (!limitedAttributes.contains(monsterAttribute))
            return Collections.emptyList();

        var value = prefixStat.getValue();
        var flatAttributeBonus = getFlatAttributeBonus(bonusAttribute, baseMonster, value);

        var attributeBonus = LimitedAttributeBonus.builder()
            .monsterAttribute(bonusAttribute.getMonsterAttribute())
            .bonus(flatAttributeBonus)
            .build();

        return List.of(attributeBonus);
    }

    private BigDecimal getFlatAttributeBonus(BonusAttribute bonusAttribute, BaseMonster baseMonster, Integer attributeBonus) {
        return switch (bonusAttribute) {
            case HIT_POINTS, ATTACK, DEFENSE -> {
                var monsterAttribute = bonusAttribute.getMonsterAttribute();
                var baseAttributeValue = baseMonster.getAttributeValue(monsterAttribute);

                yield bonusAttribute.getEffectAggregationType().calculate(attributeBonus, baseAttributeValue);
            }
            default -> BigDecimal.valueOf(attributeBonus);
        };
    }

    private String getAttributesKey(List<BonusAttribute> usefulAttributes) {
        return usefulAttributes.stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }

}
