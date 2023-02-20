package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class RuneEfficiencyInitializeService {

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    @Autowired
    private BuildStrategyService buildStrategyService;

    @Autowired
    private MainStatEfficiencyService mainStatEfficiencyService;

    @Autowired
    private PrefixStatEfficiencyService prefixStatEfficiencyService;

    @Autowired
    private SubStatEfficiencyService subStatEfficiencyService;

    @Autowired
    private RuneSetEfficiencyService runeSetEfficiencyService;

    @Autowired
    private RuneService runeService;

    public void initializeRuneEfficiencies() {
        var buildStrategies = buildStrategyService.findAll();
        var runes = runeService.findAll();

        for (BuildStrategy buildStrategy : buildStrategies) {
            for (Rune rune : runes) {
                var runeEfficiency = getRuneEfficiency(buildStrategy, rune);
                runeEfficiencyService.save(runeEfficiency);
            }
        }
        log.info("c=RuneEfficiencyInitializeService m=initializeRuneEfficiencies message=Complete");
    }

    private RuneEfficiency getRuneEfficiency(BuildStrategy buildStrategy, Rune rune) {
        var runeEfficiencyRatio = getRuneEfficiencyRatio(buildStrategy, rune);
        var maxEfficiencyRatio = getMaxEfficiencyRatio(buildStrategy, rune);

        var runeEfficiencyValue = getRuneEfficiencyValue(runeEfficiencyRatio, maxEfficiencyRatio);
        var limitedAttributeBonuses = getLimitedAttributeBonuses(buildStrategy, rune);

        var runeEfficiency = RuneEfficiency.builder()
            .rune(rune)
            .buildStrategy(buildStrategy)
            .efficiency(runeEfficiencyValue)
            .limitedAttributeBonuses(limitedAttributeBonuses)
            .build();

        runeEfficiency.getLimitedAttributeBonuses().forEach(limitedAttributeBonus -> limitedAttributeBonus.setRuneEfficiency(runeEfficiency));

        return runeEfficiency;
    }

    private BigDecimal getRuneEfficiencyValue(BigDecimal runeEfficiencyRatio, BigDecimal maxEfficiencyRatio) {
        if (maxEfficiencyRatio.equals(BigDecimal.ZERO))
            return BigDecimal.ZERO;

        return runeEfficiencyRatio
            .divide(maxEfficiencyRatio, 4, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100));
    }


    private BigDecimal getRuneEfficiencyRatio(BuildStrategy buildStrategy, Rune rune) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var requiredRuneSets = buildStrategy.getRuneSets();
        var usefulAttributes = buildStrategy.getUsefulAttributesBonus();

        var mainStatEfficiencyRatio = mainStatEfficiencyService.getMainStatEfficiencyRatio(baseMonster, rune.getMainStat().getBonusAttribute(), rune.getGrade(), usefulAttributes);
        var subStatsEfficiencyRatio = subStatEfficiencyService.getSubStatEfficiencyRatio(baseMonster, rune.getSubStats(), usefulAttributes);
        var prefixStatEfficiencyRatio = prefixStatEfficiencyService.getPrefixStatEfficiencyRatio(rune.getPrefixStat(), usefulAttributes);
        var runeSetEfficiencyRatio = runeSetEfficiencyService.getRuneSetEfficiencyRatio(rune.getSet(), requiredRuneSets, buildStrategy.getUsefulAttributes());

        return mainStatEfficiencyRatio.add(subStatsEfficiencyRatio).add(prefixStatEfficiencyRatio).add(runeSetEfficiencyRatio);
    }

    private BigDecimal getMaxEfficiencyRatio(BuildStrategy buildStrategy, Rune rune) {
        var slot = rune.getSlot();
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var remainingAttributes = new ArrayList<>(buildStrategy.getUsefulAttributesBonus());

        var maxMainStatEfficiencyRatio = mainStatEfficiencyService.getMaxMainStatFromSlot(baseMonster, slot, remainingAttributes);
        var maxSubStatsEfficiencyRatio = subStatEfficiencyService.getMaxEfficiencyRatio(baseMonster, slot, remainingAttributes);
        var maxPrefixStatEfficiencyRatio = prefixStatEfficiencyService.getMaxPrefixStatEfficiencyRatio(remainingAttributes);
        var maxRuneSetEfficiencyRatio = runeSetEfficiencyService.getMaxRuneSetEfficiency(rune.getSet().getRequirement(), buildStrategy.getRuneSets(), buildStrategy.getUsefulAttributes());

        return maxMainStatEfficiencyRatio.add(maxSubStatsEfficiencyRatio).add(maxPrefixStatEfficiencyRatio).add(maxRuneSetEfficiencyRatio);
    }

    private List<LimitedAttributeBonus> getLimitedAttributeBonuses(BuildStrategy buildStrategy, Rune rune) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var limitedAttributes = buildStrategy.getLimitedAttributes();

        var maxMainStatEfficiencyRatio = mainStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getMainStat().getBonusAttribute(), rune.getGrade(), limitedAttributes);
        var maxSubStatsEfficiencyRatio = subStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getSubStats(), limitedAttributes);
        var maxPrefixStatEfficiencyRatio = prefixStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getPrefixStat(), limitedAttributes);
        var maxRuneSetEfficiencyRatio = runeSetEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getSet(), limitedAttributes);

        return Stream.of(maxMainStatEfficiencyRatio, maxSubStatsEfficiencyRatio, maxPrefixStatEfficiencyRatio, maxRuneSetEfficiencyRatio)
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(LimitedAttributeBonus::getMonsterAttribute))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(LimitedAttributeBonus::getBonus)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)))
            .entrySet()
            .stream()
            .map(entrySet -> LimitedAttributeBonus.builder()
                .monsterAttribute(entrySet.getKey())
                .bonus(entrySet.getValue())
                .build())
            .toList();
    }

}
