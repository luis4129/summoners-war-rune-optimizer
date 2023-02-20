package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
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

    public BigDecimal getPrefixStatEfficiencyRatio(PrefixStat prefixStat, List<BonusAttribute> usefulAttributes) {
        if (!isPrefixStatUsefulAttribute(prefixStat, usefulAttributes))
            return BigDecimal.ZERO;

        var bonusAttribute = prefixStat.getBonusAttribute();

        var value = prefixStat.getValue();
        var fullyMaxedSubStatBonus = bonusAttribute.getFullyMaxedSubStatBonus();

        return BigDecimal.valueOf(value).divide(fullyMaxedSubStatBonus, 4, RoundingMode.DOWN);
    }

    public BigDecimal getMaxPrefixStatEfficiencyRatio(List<BonusAttribute> remainingUsefulAttributes) {
        var attributesKey = getAttributesKey(remainingUsefulAttributes);
        var strategyMaxEfficiencyRatio = strategyMaxEfficiencyRatioMap.get(attributesKey);

        if (Objects.isNull(strategyMaxEfficiencyRatio)) {
            var maxEfficiency = remainingUsefulAttributes
                .stream()
                .map(this::getPrefixStatEfficiencyRatio)
                .max(Comparator.comparing(BonusMaxEfficiency::getRatio))
                .orElse(BonusMaxEfficiency.builder()
                    .ratio(BigDecimal.ZERO)
                    .build());

            remainingUsefulAttributes.remove(maxEfficiency.getAttribute());
            strategyMaxEfficiencyRatioMap.put(attributesKey, maxEfficiency.getRatio());
        }

        return strategyMaxEfficiencyRatioMap.get(attributesKey);
    }

    public List<LimitedAttributeBonus> getLimitedAttributeBonuses(BaseMonster baseMonster, PrefixStat prefixStat, List<MonsterAttribute> limitedAttributes) {
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

    private boolean isPrefixStatUsefulAttribute(PrefixStat prefixStat, List<BonusAttribute> usefulAttributes) {
        return Objects.nonNull(prefixStat) && usefulAttributes.contains(prefixStat.getBonusAttribute());
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
