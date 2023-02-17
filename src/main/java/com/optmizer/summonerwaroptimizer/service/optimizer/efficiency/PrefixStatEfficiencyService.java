package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.BonusMaxEfficiency;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.StatEfficiencyRatio;
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

    public StatEfficiencyRatio getSubStatEfficiencyRatioValue(BuildStrategy buildStrategy, PrefixStat prefixStat, List<BonusAttribute> usefulAttributes, BaseMonster baseMonster) {
        var maxEfficiencyRatio = getBestPrefixStatEfficiencyRatio(usefulAttributes);

        if (Objects.isNull(prefixStat) || !buildStrategy.getUsefulAttributesBonus().contains(prefixStat.getBonusAttribute())) {
            return StatEfficiencyRatio.builder()
                .efficiencyRatio(BigDecimal.ZERO)
                .maxEfficiencyRatio(maxEfficiencyRatio)
                .build();
        }

        var bonusAttribute = prefixStat.getBonusAttribute();
        var efficiencyRatio = getPrefixStatEfficiencyRatio(bonusAttribute, prefixStat.getValue());


        return StatEfficiencyRatio.builder()
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

        var maxEfficiencyRatio = maxTotalSubStatBonus.divide(fullyMaxedSubStatBonus, 3, RoundingMode.DOWN);
        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(maxEfficiencyRatio)
            .build();
    }

    private BigDecimal getPrefixStatEfficiencyRatio(BonusAttribute attribute, Integer value) {
        var fullyMaxedSubStatBonus = attribute.getFullyMaxedSubStatBonus();

        return BigDecimal.valueOf(value).divide(fullyMaxedSubStatBonus, 3, RoundingMode.DOWN);
    }

    private String getAttributesKey(List<BonusAttribute> usefulAttributes) {
        return usefulAttributes.stream()
            .map(Enum::name)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.joining("|"));
    }

}
