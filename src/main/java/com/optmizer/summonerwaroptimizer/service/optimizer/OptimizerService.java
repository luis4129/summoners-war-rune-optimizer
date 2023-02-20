package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.exception.MoreEfficientRuneKeyNotFoundException;
import com.optmizer.summonerwaroptimizer.exception.NoPossibleBuildException;
import com.optmizer.summonerwaroptimizer.exception.SlotWithNoRunesException;
import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreferenceType;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildSimulation;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.optimizer.efficiency.RuneEfficiencyService;
import com.optmizer.summonerwaroptimizer.service.simulation.MonsterBuildService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OptimizerService {

    @Autowired
    private BuildStrategyService buildStrategyService;

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    @Autowired
    private MonsterBuildService monsterBuildService;

    @Autowired
    private AttributeEfficiencyService attributeEfficiencyService;

    @Autowired
    private RuneSetOptimizerService runeSetOptimizerService;

    private final List<Rune> equippedRunes = new ArrayList<>();
    private static final BigDecimal RUNES_IN_BUILD = BigDecimal.valueOf(6);

    public List<BuildSimulation> optimize() {
        return buildStrategyService.findAll()
            .stream()
            .map(this::optimize)
            .toList();
    }

    @Transactional
    public BuildSimulation optimize(BuildStrategy buildStrategy) {
        var monster = buildStrategy.getMonster();
        var baseMonster = buildStrategy.getMonster().getBaseMonster();

        log.info("c=OptimizerService m=optimize message=Build optimization started for monster {}", baseMonster.getName());

        var requiredRuneSets = buildStrategy.getRuneSets().stream().filter(runeSet -> !runeSet.equals(RuneSet.ANY)).toList();
        var runeEfficiencies = runeEfficiencyService.findByMonsterSwarfarmId(monster.getSwarfarmId()).stream().filter(runeEfficiency -> !equippedRunes.contains(runeEfficiency.getRune())).toList();
        var onlyRequiredValuePreferences = buildStrategy.getBuildPreferences().stream().filter(preference -> preference.getType().equals(BuildPreferenceType.WITHIN_REQUIRED_RANGE)).toList();
        var attributeEfficiencyBySlotByAttributeMap = attributeEfficiencyService.getAttributeEfficiencyBySlotByAttributeMap(onlyRequiredValuePreferences, runeEfficiencies);

        return getBestBuildSimulation(runeEfficiencies, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
    }

    public BuildSimulation getBestBuildSimulation(List<RuneEfficiency> runeEfficiencies, BaseMonster baseMonster, List<RuneSet> requiredRuneSets,
                                                  Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var slotRuneEfficiencies = runeEfficiencies.stream().collect(Collectors.groupingBy(runeEfficiency -> runeEfficiency.getRune().getSlot()));
        var mostEfficientRunes = getMostEfficientRunes(slotRuneEfficiencies);
        var mostEfficientRunesWithRequiredSets = runeSetOptimizerService.getMostEfficientRunesWithRequiredSets(mostEfficientRunes, slotRuneEfficiencies, requiredRuneSets);
        var bestBuildSimulation = getBestBuildSimulationRecursively(mostEfficientRunesWithRequiredSets, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
        equippedRunes.addAll(bestBuildSimulation.getBuild().getRunes());
        return bestBuildSimulation;
    }

    private Map<Integer, RuneEfficiency> getMostEfficientRunes(Map<Integer, List<RuneEfficiency>> slotRuneEfficienciesMap) {
        return slotRuneEfficienciesMap.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue()
                    .stream()
                    .max(Comparator.comparing(RuneEfficiency::getEfficiency))
                    .orElseThrow(SlotWithNoRunesException::new)));
    }

    private BuildSimulation getBestBuildSimulationRecursively(Map<Integer, RuneEfficiency> currentRunes, BaseMonster baseMonster, List<RuneSet> requiredRuneSets,
                                                              Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var runes = currentRunes.values().stream().map(RuneEfficiency::getRune).toList();
        var buildEfficiency = currentRunes.values().stream().map(RuneEfficiency::getEfficiency).reduce(BigDecimal.ZERO, BigDecimal::add).divide(RUNES_IN_BUILD, 2, RoundingMode.DOWN);
        var build = Build.builder().runes(runes).efficiency(buildEfficiency).build();
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, build);
        var currentRuneSets = runes.stream().collect(Collectors.groupingBy(Rune::getSet));
        var onlyRequiredValuePreferences = attributeEfficiencyBySlotByAttributeMap.keySet().stream().toList();

        var possibleUnmatchedPreferenceMinimumValue = onlyRequiredValuePreferences.stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
            .filter(buildPreference -> buildPreference.getMinimumValue() > monsterStats.getAttributeValue(buildPreference.getAttribute()))
            .findFirst();

        if (possibleUnmatchedPreferenceMinimumValue.isPresent()) {
            var unmatchedPreference = possibleUnmatchedPreferenceMinimumValue.get();
            var higherAttributeRune = getRuneWithHigherAttributeValue(unmatchedPreference, currentRunes, monsterStats, baseMonster, onlyRequiredValuePreferences, attributeEfficiencyBySlotByAttributeMap.get(unmatchedPreference), currentRuneSets, requiredRuneSets);
            currentRunes.put(higherAttributeRune.getRune().getSlot(), higherAttributeRune);
            return getBestBuildSimulationRecursively(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
        }

        var possibleUnmatchedPreferenceMaximumValue = onlyRequiredValuePreferences.stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMaximumValue()))
            .filter(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) > buildPreference.getMaximumValue())
            .findFirst();

        if (possibleUnmatchedPreferenceMaximumValue.isPresent()) {
            var unmatchedPreference = possibleUnmatchedPreferenceMaximumValue.get();
            var lowerAttributeRune = getRuneWithLowerAttributeValue(unmatchedPreference, currentRunes, monsterStats, baseMonster, onlyRequiredValuePreferences, attributeEfficiencyBySlotByAttributeMap.get(unmatchedPreference), currentRuneSets, requiredRuneSets);
            currentRunes.put(lowerAttributeRune.getRune().getSlot(), lowerAttributeRune);
            return getBestBuildSimulationRecursively(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
        }

        return BuildSimulation.builder()
            .monsterName(baseMonster.getName())
            .monsterStats(monsterStats)
            .build(build)
            .build();
    }

    private RuneEfficiency getRuneWithHigherAttributeValue(BuildPreference unmatchedRequiredValuePreference, Map<Integer, RuneEfficiency> currentRunes, MonsterStats monsterStats, BaseMonster baseMonster,
                                                           List<BuildPreference> onlyRequiredValuePreferences, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> slotEfficiencyMap,
                                                           Map<RuneSet, List<Rune>> currentRuneSets, List<RuneSet> requiredRuneSets) {
        var belowMinimumAttribute = unmatchedRequiredValuePreference.getAttribute();
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var requiredRuneSetsThatCanBeLost = getRequiredRuneSetsThatCanBeLost(currentRunes, requiredRuneSets);

        var highestAttributeGainRatio = BigDecimal.ZERO;
        var highestAttributeGainRatioSlot = 0;
        var highestAttributeGainRatioRune = new RuneEfficiency();
        var isEfficiencyDifferencePositive = false;

        for (var slot : slotEfficiencyMap.keySet()) {
            try {
                var currentRune = currentRunes.get(slot);
                var currentRuneEfficiency = currentRune.getEfficiency();
                var currentRuneAttributeBonus = currentRune.getLimitedAttributeBonusValue(belowMinimumAttribute);
                var minimumAttributeValues = onlyRequiredValuePreferences.stream()
                    .filter(buildPreference -> !buildPreference.equals(unmatchedRequiredValuePreference))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValue = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        return currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute()).subtract(BigDecimal.valueOf(extraValue));
                    }));

                var slotMap = slotEfficiencyMap.get(slot);
                var higherAttributeBonus = getHigherAttributeWithoutMessingOthers(slotMap, currentRune, unmatchedRequiredValuePreference, minimumAttributeValues, runeSetsBonusesThaCanBeLost, requiredRuneSetsThatCanBeLost);

                var nextRune = slotMap.get(higherAttributeBonus);
                var nextRuneEfficiency = nextRune.getEfficiency();
                var attributeGain = higherAttributeBonus.subtract(currentRuneAttributeBonus);

                if (isEfficiencyDifferencePositive || nextRuneEfficiency.compareTo(currentRuneEfficiency) >= 0) {
                    if (!isEfficiencyDifferencePositive) {
                        isEfficiencyDifferencePositive = true;
                        highestAttributeGainRatio = BigDecimal.valueOf(-1);
                    }

                    var efficiencyGain = nextRuneEfficiency.subtract(currentRuneEfficiency);

                    var attributeEfficiencyGainRatio = BigDecimal.ZERO;

                    if (efficiencyGain.compareTo(BigDecimal.ZERO) > 0)
                        attributeEfficiencyGainRatio = attributeGain.multiply(efficiencyGain);

                    if (attributeEfficiencyGainRatio.compareTo(highestAttributeGainRatio) > 0) {
                        highestAttributeGainRatio = attributeEfficiencyGainRatio;
                        highestAttributeGainRatioSlot = slot;
                        highestAttributeGainRatioRune = nextRune;
                    }
                } else {
                    var efficiencyLoss = currentRuneEfficiency.subtract(nextRuneEfficiency);

                    var attributeGainPerEfficiencyLossRatio = attributeGain.divide(efficiencyLoss, 4, RoundingMode.UP);

                    if (attributeGainPerEfficiencyLossRatio.compareTo(highestAttributeGainRatio) > 0) {
                        highestAttributeGainRatio = attributeGainPerEfficiencyLossRatio;
                        highestAttributeGainRatioSlot = slot;
                        highestAttributeGainRatioRune = nextRune;
                    }
                }
            } catch (MoreEfficientRuneKeyNotFoundException ignored) {
            }
        }

        if (highestAttributeGainRatioSlot == 0) throw new NoPossibleBuildException();

        return highestAttributeGainRatioRune;
    }

    private RuneEfficiency getRuneWithLowerAttributeValue(BuildPreference unmatchedRequiredValuePreference, Map<Integer, RuneEfficiency> currentRunes, MonsterStats monsterStats, BaseMonster baseMonster,
                                                          List<BuildPreference> onlyRequiredValuePreferences, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> slotEfficiencyMap,
                                                          Map<RuneSet, List<Rune>> currentRuneSets, List<RuneSet> requiredRuneSets) {
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var requiredRuneSetsThatCanBeLost = getRequiredRuneSetsThatCanBeLost(currentRunes, requiredRuneSets);

        var highestEfficiency = BigDecimal.ZERO;
        var highestEfficiencySlot = 0;
        var highestEfficiencyRune = new RuneEfficiency();

        for (var slot : slotEfficiencyMap.keySet()) {
            try {
                var currentRune = currentRunes.get(slot);
                var currentRuneEfficiency = currentRune.getEfficiency();

                var minimumAttributeValues = onlyRequiredValuePreferences.stream()
                    .filter(buildPreference -> !buildPreference.equals(unmatchedRequiredValuePreference))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValue = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        return currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute()).subtract(BigDecimal.valueOf(extraValue));
                    }));

                var maximumAttributeValues = onlyRequiredValuePreferences.stream()
                    .filter(buildPreference -> !buildPreference.equals(unmatchedRequiredValuePreference))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValue = buildPreference.getMaximumValue() - monsterStats.getAttributeValue(buildPreference.getAttribute());
                        return currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute()).subtract(BigDecimal.valueOf(extraValue));
                    }));

                var slotMap = slotEfficiencyMap.get(slot);

                var lowerAttributeBonus = getLowerAttributeWithoutMessingOthers(slotMap, currentRune, unmatchedRequiredValuePreference, minimumAttributeValues, maximumAttributeValues, runeSetsBonusesThaCanBeLost, requiredRuneSetsThatCanBeLost);

                var nextRune = slotMap.get(lowerAttributeBonus);
                var nextRuneEfficiency = nextRune.getEfficiency();
                var efficiencyGain = nextRuneEfficiency.subtract(currentRuneEfficiency);

                if (highestEfficiencySlot == 0 || efficiencyGain.compareTo(highestEfficiency) > 0) {
                    highestEfficiency = efficiencyGain;
                    highestEfficiencySlot = slot;
                    highestEfficiencyRune = nextRune;
                }

            } catch (MoreEfficientRuneKeyNotFoundException ignored) {
            }
        }

        if (highestEfficiencySlot == 0) throw new NoPossibleBuildException();

        return highestEfficiencyRune;
    }

    private List<RuneSet> getRequiredRuneSetsThatCanBeLost(Map<Integer, RuneEfficiency> currentRunes, List<RuneSet> requiredRuneSets) {
        return requiredRuneSets.stream()
            .collect(Collectors.groupingBy(Function.identity()))
            .entrySet()
            .stream()
            .filter(entry -> {
                var requiredRuneSet = entry.getKey();
                var requiredRuneSetCount = entry.getValue().size();
                var currentRunesWithThatSet = Math.toIntExact(currentRunes.values().stream().filter(runeEfficiency -> runeEfficiency.getRune().getSet().equals(requiredRuneSet)).count());
                return (requiredRuneSet.getRequirement() * requiredRuneSetCount) == currentRunesWithThatSet;
            })
            .map(Map.Entry::getKey)
            .toList();
    }

    private Map<RuneSet, BigDecimal> getRuneSetBonusesThatCanBeLost(Map<RuneSet, List<Rune>> currentRuneSets, BaseMonster baseMonster) {
        return currentRuneSets.entrySet()
            .stream()
            .filter(entry -> entry.getKey().getBonusValue() > 0)
            .filter(entry -> entry.getValue().size() % entry.getKey().getRequirement() == 0)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getKey().calculateBonusEffect(baseMonster.getAttributeValue(entry.getKey().getAttribute()))
            ));
    }

    private BigDecimal getHigherAttributeWithoutMessingOthers(TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference unmatchedPreference,
                                                              Map<MonsterAttribute, BigDecimal> minimumAttributeValues,
                                                              Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeLost, List<RuneSet> requiredRuneSetAboutToBeLost) {
        var currentAttribute = unmatchedPreference.getAttribute();
        var runeSet = currentRune.getRune().getSet();
        var canLoseRuneSetBonus = bonusRuneSetsAboutToBeLost.containsKey(runeSet);
        var canLoseRequiredSet = requiredRuneSetAboutToBeLost.contains(runeSet);
        var lowerAttribute = currentRune.getLimitedAttributeBonusValue(currentAttribute);

        while (true) {
            var rune = attributeEfficiencyMap.higherEntry(lowerAttribute);

            if (rune == null)
                throw new MoreEfficientRuneKeyNotFoundException();

            lowerAttribute = rune.getKey();

            if (canLoseRequiredSet && !runeSet.equals(rune.getValue().getRune().getSet()))
                continue;

            if (canLoseRuneSetBonus && !runeSet.equals(rune.getValue().getRune().getSet()) && minimumAttributeValues.containsKey(runeSet.getAttribute())) {
                var currentMinimumValue = minimumAttributeValues.get(runeSet.getAttribute());
                var newMinimumValue = currentMinimumValue.add(bonusRuneSetsAboutToBeLost.get(runeSet));
                minimumAttributeValues.put(runeSet.getAttribute(), newMinimumValue);
            }

            if (isGoingBelowMinimumValues(minimumAttributeValues, rune.getValue())) {
                continue;
            }

            return lowerAttribute;
        }
    }

    private BigDecimal getLowerAttributeWithoutMessingOthers(TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference unmatchedPreference,
                                                             Map<MonsterAttribute, BigDecimal> minimumAttributeValues, Map<MonsterAttribute, BigDecimal> maximumAttributeValues,
                                                             Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeLost, List<RuneSet> requiredRuneSetAboutToBeLost) {
        var currentAttribute = unmatchedPreference.getAttribute();
        var runeSet = currentRune.getRune().getSet();
        var canLoseRuneSetBonus = bonusRuneSetsAboutToBeLost.containsKey(runeSet);
        var canLoseRequiredSet = requiredRuneSetAboutToBeLost.contains(runeSet);
        var lowerAttribute = currentRune.getLimitedAttributeBonusValue(currentAttribute);

        while (true) {
            var rune = attributeEfficiencyMap.lowerEntry(lowerAttribute);

            if (rune == null)
                throw new MoreEfficientRuneKeyNotFoundException();

            lowerAttribute = rune.getKey();

            if (canLoseRequiredSet && !runeSet.equals(rune.getValue().getRune().getSet()))
                continue;

            if (canLoseRuneSetBonus && !runeSet.equals(rune.getValue().getRune().getSet()) && maximumAttributeValues.containsKey(runeSet.getAttribute())) {
                var currentMinimumValue = minimumAttributeValues.get(runeSet.getAttribute());
                var newMinimumValue = currentMinimumValue.add(bonusRuneSetsAboutToBeLost.get(runeSet));
                minimumAttributeValues.put(runeSet.getAttribute(), newMinimumValue);
            }

            if (isGoingBelowMinimumValues(minimumAttributeValues, rune.getValue()) || isGoingAboveMaximumValue(minimumAttributeValues, rune.getValue())) {
                continue;
            }

            return lowerAttribute;
        }
    }

    private boolean isGoingBelowMinimumValues(Map<MonsterAttribute, BigDecimal> minimumAttributeValues, RuneEfficiency runeEfficiency) {
        return minimumAttributeValues.entrySet().stream().anyMatch(entrySet -> {
            var monsterAttribute = entrySet.getKey();
            var minimumValue = entrySet.getValue();
            var actualValue = runeEfficiency.getLimitedAttributeBonusValue(monsterAttribute);
            return actualValue.compareTo(minimumValue) < 0;
        });
    }

    private boolean isGoingAboveMaximumValue(Map<MonsterAttribute, BigDecimal> maximumAttributeValues, RuneEfficiency runeEfficiency) {
        return maximumAttributeValues.entrySet().stream().anyMatch(entrySet -> {
            var monsterAttribute = entrySet.getKey();
            var maximumValue = entrySet.getValue();
            var actualValue = runeEfficiency.getLimitedAttributeBonusValue(monsterAttribute);
            return maximumValue.compareTo(actualValue) < 0;
        });
    }
}
