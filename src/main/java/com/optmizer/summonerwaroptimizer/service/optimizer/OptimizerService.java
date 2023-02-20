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
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
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
    private static final List<BuildPreference> failsInARow = new ArrayList<>();

    public List<BuildSimulation> optimize() {
        return buildStrategyService.findAll()
            .stream()
            .map(this::optimize)
            .toList();
    }

    @Transactional
    public BuildSimulation optimize(BuildStrategy buildStrategy) {
        //APROXIMATE VALUE INSTEAD OF REMAIN ABOVE IT
        failsInARow.clear();
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
                    .max(Comparator.comparing(RuneEfficiency::getCompleteEfficiency))
                    .orElseThrow(SlotWithNoRunesException::new)));
    }

    private BuildSimulation getBestBuildSimulationRecursively(Map<Integer, RuneEfficiency> currentRunes, BaseMonster baseMonster, List<RuneSet> requiredRuneSets,
                                                              Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var runes = currentRunes.values().stream().map(RuneEfficiency::getRune).toList();
        var buildEfficiency = currentRunes.values().stream().map(RuneEfficiency::getCompleteEfficiency).reduce(BigDecimal.ZERO, BigDecimal::add).divide(RUNES_IN_BUILD, 2, RoundingMode.DOWN);
        var build = Build.builder().runes(runes).efficiency(buildEfficiency).build();
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, build);
        var currentRuneSets = runes.stream().collect(Collectors.groupingBy(Rune::getSet));
        var onlyRequiredValuePreferences = attributeEfficiencyBySlotByAttributeMap.keySet().stream().toList();

        var possibleUnmatchedRequiredValuePreference = onlyRequiredValuePreferences.stream().filter(buildPreference -> buildPreference.getMinimumValue() > monsterStats.getAttributeValue(buildPreference.getAttribute())).findFirst();
        if (possibleUnmatchedRequiredValuePreference.isPresent()) {
            var unmatchedRequiredValuePreference = possibleUnmatchedRequiredValuePreference.get();
            var attributeEfficiencyBySlotMap = attributeEfficiencyBySlotByAttributeMap.get(unmatchedRequiredValuePreference);

            var higherAttributeRune = getRuneWithHigherAttributeValue(unmatchedRequiredValuePreference, currentRunes, monsterStats, baseMonster, onlyRequiredValuePreferences, attributeEfficiencyBySlotMap, currentRuneSets, requiredRuneSets);

            currentRunes.put(higherAttributeRune.getRune().getSlot(), higherAttributeRune);
            return getBestBuildSimulationRecursively(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
        }

        log.info("c=OptimizerService m=optimize message=Prerequisites have been met, starting to optimize efficiency");
        return getMostEfficientBuildSimulationWithoutLosingRequirements(currentRunes, baseMonster, attributeEfficiencyBySlotByAttributeMap, requiredRuneSets);
    }

    private BuildSimulation getMostEfficientBuildSimulationWithoutLosingRequirements(Map<Integer, RuneEfficiency> currentRunes, BaseMonster baseMonster,
                                                                                     Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap,
                                                                                     List<RuneSet> requiredRuneSets) {
        var runes = currentRunes.values().stream().map(RuneEfficiency::getRune).toList();
        var buildEfficiency = currentRunes.values().stream().map(RuneEfficiency::getCompleteEfficiency).reduce(BigDecimal.ZERO, BigDecimal::add).divide(RUNES_IN_BUILD, 2, RoundingMode.DOWN);
        var build = Build.builder().runes(runes).efficiency(buildEfficiency).build();
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, build);
        var onlyRequiredValuePreferences = attributeEfficiencyBySlotByAttributeMap.keySet().stream().toList();

        var highestAttributeWaste = onlyRequiredValuePreferences.stream()
            .filter(buildPreference -> !failsInARow.contains(buildPreference))
            .collect(Collectors.toMap(Function.identity(), buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue()))
            .entrySet()
            .stream()
            .max(Comparator.comparing(entry -> {
                var preference = entry.getKey();
                var extraValue = entry.getValue();
                var bonusAttribute = BonusAttribute.valueOf(preference.getAttribute().name());
                var flatBonus = getFlatAttributeBonus(bonusAttribute, baseMonster, bonusAttribute.getFullyMaxedSubStatBonus());
                return BigDecimal.valueOf(extraValue).divide(flatBonus, 4, RoundingMode.DOWN);
            }))
            .map(Map.Entry::getKey).get();


        return getMostEfficientBuildSimulationByPreference(currentRunes, baseMonster, highestAttributeWaste, monsterStats, attributeEfficiencyBySlotByAttributeMap, requiredRuneSets);
    }

    private BuildSimulation getMostEfficientBuildSimulationByPreference(Map<Integer, RuneEfficiency> currentRunes, BaseMonster baseMonster, BuildPreference currentBuildPreference, MonsterStats monsterStats,
                                                                        Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap,
                                                                        List<RuneSet> requiredRuneSets) {

        var runes = currentRunes.values().stream().map(RuneEfficiency::getRune).toList();
        var buildEfficiency = currentRunes.values().stream().map(RuneEfficiency::getCompleteEfficiency).reduce(BigDecimal.ZERO, BigDecimal::add).divide(RUNES_IN_BUILD, 2, RoundingMode.DOWN);
        var build = Build.builder().runes(runes).efficiency(buildEfficiency).build();
        var currentPreferenceAttribute = currentBuildPreference.getAttribute();
        var onlyRequiredValuePreferences = attributeEfficiencyBySlotByAttributeMap.keySet().stream().toList();
        var highestPriority = onlyRequiredValuePreferences.stream().map(BuildPreference::getPriority).max(Comparator.naturalOrder()).orElse(0);

        var currentRuneSets = runes.stream().collect(Collectors.groupingBy(Rune::getSet));

        var attributeEfficiencyBySlotMap = attributeEfficiencyBySlotByAttributeMap.get(currentBuildPreference);
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var requiredRuneSetsThatCanBeLost = getRequiredRuneSetsThatCanBeLost(currentRunes, requiredRuneSets);

        var highestRatio = BigDecimal.ZERO;
        var highestRatioSlot = 0;
        RuneEfficiency highestRatioRune = null;

        for (var slot : attributeEfficiencyBySlotMap.keySet()) {
            try {
                var currentRune = currentRunes.get(slot);
                var currentRuneEfficiency = currentRune.getEfficiencyByPriority(highestPriority);
                var currentRuneAttributeBonus = currentRune.getLimitedAttributeBonusValue(currentPreferenceAttribute);

                var minimumAttributeValues = onlyRequiredValuePreferences.stream()
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValue = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        return currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute()).subtract(BigDecimal.valueOf(extraValue));
                    }));

                var attributeEfficiencyMap = attributeEfficiencyBySlotMap.get(slot);
                var moreEfficientRuneAttributeBonus = getMoreEfficientRuneKey(attributeEfficiencyMap, currentRune, currentBuildPreference, minimumAttributeValues, runeSetsBonusesThaCanBeLost, requiredRuneSetsThatCanBeLost);
                var moreEfficientRune = attributeEfficiencyMap.get(moreEfficientRuneAttributeBonus);

                var attributeLoss = currentRuneAttributeBonus.subtract(moreEfficientRuneAttributeBonus);
                var efficiencyGain = moreEfficientRune.getEfficiencyByPriority(highestPriority).subtract(currentRuneEfficiency);

                var efficiencyGainPerAttributeLossRatio = efficiencyGain.divide(attributeLoss, 4, RoundingMode.UP);

                if (efficiencyGainPerAttributeLossRatio.compareTo(highestRatio) > 0) {
                    highestRatio = efficiencyGainPerAttributeLossRatio;
                    highestRatioSlot = slot;
                    highestRatioRune = moreEfficientRune;
                }
            } catch (MoreEfficientRuneKeyNotFoundException ignored) {
            }
        }

        if (stillCanUpgradeLoweringExtraAttributesPreference(highestRatioSlot)) {
            log.info("c=OptimizerService m=getBestBuildSimulation message=Replacing rune, current status: {}", monsterStats);
            failsInARow.clear();
            currentRunes.put(highestRatioSlot, highestRatioRune);
        } else {
            failsInARow.add(currentBuildPreference);
        }

        if (failsInARow.size() == onlyRequiredValuePreferences.size())
            return BuildSimulation.builder()
                .monsterName(baseMonster.getName())
                .monsterStats(monsterStats)
                .build(build)
                .build();

        return getMostEfficientBuildSimulationWithoutLosingRequirements(currentRunes, baseMonster, attributeEfficiencyBySlotByAttributeMap, requiredRuneSets);
    }

    private BigDecimal getFlatAttributeBonus(BonusAttribute bonusAttribute, BaseMonster baseMonster, BigDecimal attributeBonus) {
        return switch (bonusAttribute) {
            case HIT_POINTS, ATTACK, DEFENSE -> {
                var monsterAttribute = bonusAttribute.getMonsterAttribute();
                var baseAttributeValue = baseMonster.getAttributeValue(monsterAttribute);

                yield bonusAttribute.getEffectAggregationType().calculate(attributeBonus.intValue(), baseAttributeValue);
            }
            default -> attributeBonus;
        };
    }

    private boolean stillCanUpgradeLoweringExtraAttributesPreference(Integer highestRatioSlot) {
        return highestRatioSlot != 0;
    }

    private RuneEfficiency getRuneWithHigherAttributeValue(BuildPreference unmatchedRequiredValuePreference, Map<Integer, RuneEfficiency> currentRunes, MonsterStats monsterStats, BaseMonster baseMonster,
                                                           List<BuildPreference> onlyRequiredValuePreferences, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> missingAttributeEfficiencyBySlotMap,
                                                           Map<RuneSet, List<Rune>> currentRuneSets, List<RuneSet> requiredRuneSets) {
        var missingAttribute = unmatchedRequiredValuePreference.getAttribute();
        var priority = unmatchedRequiredValuePreference.getPriority();
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var requiredRuneSetsThatCanBeLost = getRequiredRuneSetsThatCanBeLost(currentRunes, requiredRuneSets);

        var highestAttributeGainRatio = BigDecimal.ZERO;
        var highestAttributeGainRatioSlot = 0;
        var isEfficiencyDifferencePositive = false;
        var highestAttributeGainRatioRune = new RuneEfficiency();

        for (var slot : missingAttributeEfficiencyBySlotMap.keySet()) {
            try {
                var currentRune = currentRunes.get(slot);
                var currentRuneEfficiency = currentRune.getEfficiencyByPriority(priority);
                var currentRuneAttributeBonus = currentRune.getLimitedAttributeBonusValue(missingAttribute);
                var minimumAttributeValues = onlyRequiredValuePreferences.stream()
                    .filter(buildPreference -> !buildPreference.equals(unmatchedRequiredValuePreference))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValue = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        return currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute()).subtract(BigDecimal.valueOf(extraValue));
                    }));

                var slotMap = missingAttributeEfficiencyBySlotMap.get(slot);
                var higherAttributeBonus = getHigherAttributeWithoutLosingPreviousOnes(slotMap, currentRune, unmatchedRequiredValuePreference, minimumAttributeValues, runeSetsBonusesThaCanBeLost, requiredRuneSetsThatCanBeLost);

                var nextRune = slotMap.get(higherAttributeBonus);
                var nextRuneEfficiency = nextRune.getEfficiencyByPriority(priority);
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

    private BigDecimal getHigherAttributeWithoutLosingPreviousOnes(TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference unmatchedPreference,
                                                                   Map<MonsterAttribute, BigDecimal> minimumAttributeValues, Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeLost,
                                                                   List<RuneSet> requiredRuneSetAboutToBeLost) {
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

            if (isLosingPreviousAttributeRequirements(minimumAttributeValues, rune.getValue())) {
                continue;
            }

            return lowerAttribute;
        }
    }

    private boolean isLosingPreviousAttributeRequirements(Map<MonsterAttribute, BigDecimal> minimumAttributeValues, RuneEfficiency runeEfficiency) {
        return minimumAttributeValues.entrySet().stream().anyMatch(entrySet -> {
            var monsterAttribute = entrySet.getKey();
            var minimumValue = entrySet.getValue();
            var actualValue = runeEfficiency.getLimitedAttributeBonusValue(monsterAttribute);
            return actualValue.compareTo(minimumValue) < 0;
        });
    }

    private BigDecimal getMoreEfficientRuneKey(TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference currentBuildPreference,
                                               Map<MonsterAttribute, BigDecimal> minimumAttributeValues, Map<RuneSet, BigDecimal> bonusRuneSetsThatCanBeLost,
                                               List<RuneSet> requiredRuneSetsThaCanBeLost) {

        var priority = currentBuildPreference.getPriority();
        var currentAttribute = currentBuildPreference.getAttribute();
        var lowerAttribute = currentRune.getLimitedAttributeBonusValue(currentAttribute);
        var runeSet = currentRune.getRune().getSet();
        var canLoseRuneSetBonus = bonusRuneSetsThatCanBeLost.containsKey(runeSet);
        var canLoseRequiredSet = requiredRuneSetsThaCanBeLost.contains(runeSet);
        var currentEfficiency = currentRune.getEfficiencyByPriority(currentBuildPreference.getPriority());
        var efficiencyGain = BigDecimal.ZERO;

        while (notGainingEfficiency(efficiencyGain)) {
            var rune = attributeEfficiencyMap.lowerEntry(lowerAttribute);

            if (rune == null)
                throw new MoreEfficientRuneKeyNotFoundException();

            lowerAttribute = rune.getKey();

            if (canLoseRequiredSet && !runeSet.equals(rune.getValue().getRune().getSet()))
                continue;

            if (canLoseRuneSetBonus && !runeSet.equals(rune.getValue().getRune().getSet()) && minimumAttributeValues.containsKey(runeSet.getAttribute())) {
                var currentMinimumValue = minimumAttributeValues.get(runeSet.getAttribute());
                var newMinimumValue = currentMinimumValue.add(bonusRuneSetsThatCanBeLost.get(runeSet));
                log.info("c=OptimizeService m=getHigherAttributeWithoutLosingPreviousOnes message= Losing set {}, with a bonus of {}. Minimum value went from {} to {}", runeSet, bonusRuneSetsThatCanBeLost.get(runeSet), currentMinimumValue, newMinimumValue);
                minimumAttributeValues.put(runeSet.getAttribute(), newMinimumValue);
            }

            if (isLosingPreviousAttributeRequirements(minimumAttributeValues, rune.getValue())) {
                continue;
            }

            efficiencyGain = rune.getValue().getEfficiencyByPriority(priority).subtract(currentEfficiency);
        }

        return lowerAttribute;
    }

    private boolean notGainingEfficiency(BigDecimal efficiencyGain) {
        return efficiencyGain.compareTo(BigDecimal.ZERO) <= 0;
    }
}
