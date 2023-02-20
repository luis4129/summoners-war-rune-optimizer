package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.*;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
        var usefulMonsterAttributesByPriorityMap = buildStrategyService.getUsefulAttributesByPriorityMap(buildStrategy);
        var usefulBonusAttributesByPriorityMap = buildStrategyService.getUsefulAttributesBonusByPriorityMap(buildStrategy);
        var remainingAttributesByPriorityMap = buildStrategyService.getUsefulAttributesBonusByPriorityMap(buildStrategy);
        var limitedAttributes = buildStrategyService.getLimitedAttributes(buildStrategy);
        var baseMonster = buildStrategy.getMonster().getBaseMonster();

        var mainStatEfficiencyRatio = mainStatEfficiencyService.getSubStatEfficiencyRatio(baseMonster, rune, usefulBonusAttributesByPriorityMap, limitedAttributes, remainingAttributesByPriorityMap);
        var subStatsEfficiencyRatio = subStatEfficiencyService.getSubStatEfficiencyRatio(baseMonster, rune, usefulBonusAttributesByPriorityMap, limitedAttributes, remainingAttributesByPriorityMap);
        var prefixStatEfficiencyRatio = prefixStatEfficiencyService.getSubStatEfficiencyRatioValue(baseMonster, rune.getPrefixStat(), usefulBonusAttributesByPriorityMap, limitedAttributes, remainingAttributesByPriorityMap);
        var runeSetEfficiencyRatio = runeSetEfficiencyService.getSubStatEfficiencyRatio(baseMonster, rune.getSet(), usefulMonsterAttributesByPriorityMap, limitedAttributes, buildStrategy.getRuneSets());

        var priorityEfficiencies = new ArrayList<PriorityEfficiency>();

        for (var priority = 0; priority < usefulBonusAttributesByPriorityMap.size(); priority++) {
            var priorityEfficiencyRatio = sumEfficiencies(priority, mainStatEfficiencyRatio, subStatsEfficiencyRatio, prefixStatEfficiencyRatio, runeSetEfficiencyRatio);
            var priorityMaxEfficiencyRatio = sumMaxEfficiencies(priority, mainStatEfficiencyRatio, subStatsEfficiencyRatio, prefixStatEfficiencyRatio, runeSetEfficiencyRatio);

            var priorityEfficiencyValue = BigDecimal.ZERO.equals(priorityMaxEfficiencyRatio) ?
                BigDecimal.ZERO :
                priorityEfficiencyRatio.divide(priorityMaxEfficiencyRatio, 4, RoundingMode.DOWN).multiply(BigDecimal.valueOf(100));

            var priorityEfficiency = PriorityEfficiency.builder()
                .priority(priority)
                .efficiency(priorityEfficiencyValue)
                .build();

            priorityEfficiencies.add(priorityEfficiency);
        }

        var limitedAttributeBonuses = getLimitedAttributeBonuses(mainStatEfficiencyRatio,
            subStatsEfficiencyRatio,
            prefixStatEfficiencyRatio,
            runeSetEfficiencyRatio);

        var runeEfficiency = RuneEfficiency.builder()
            .rune(rune)
            .buildStrategy(buildStrategy)
            .completeEfficiency(priorityEfficiencies.get(0).getEfficiency())
            .priorityEfficiencies(priorityEfficiencies)
            .limitedAttributeBonuses(limitedAttributeBonuses)
            .build();

        runeEfficiency.getLimitedAttributeBonuses().forEach(limitedAttributeBonus -> limitedAttributeBonus.setRuneEfficiency(runeEfficiency));
        runeEfficiency.getPriorityEfficiencies().forEach(efficiency -> efficiency.setRuneEfficiency(runeEfficiency));

        return runeEfficiency;
    }

    private BigDecimal sumEfficiencies(Integer priority, RuneEfficiencyRatio... runeEfficiencyRatios) {
        return Arrays.stream(runeEfficiencyRatios)
            .map(RuneEfficiencyRatio::getPriorityEfficiencyRatios)
            .map(priorityEfficiencyRatios -> priorityEfficiencyRatios.get(priority))
            .map(PriorityEfficiencyRatio::getEfficiencyRatio)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumMaxEfficiencies(Integer priority, RuneEfficiencyRatio... runeEfficiencyRatios) {
        return Arrays.stream(runeEfficiencyRatios)
            .map(RuneEfficiencyRatio::getPriorityEfficiencyRatios)
            .map(priorityEfficiencyRatios -> priorityEfficiencyRatios.get(priority))
            .map(PriorityEfficiencyRatio::getMaxEfficiencyRatio)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<LimitedAttributeBonus> getLimitedAttributeBonuses(RuneEfficiencyRatio... runes) {
        return Stream.of(runes)
            .map(RuneEfficiencyRatio::getLimitedAttributeBonuses)
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
