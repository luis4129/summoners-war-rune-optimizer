package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.exception.*;
import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildSimulation;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.optimizer.efficiency.BuildEfficiencyService;
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
import java.util.stream.Stream;

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

    @Autowired
    private BuildEfficiencyService buildEfficiencyService;

    private static final List<Rune> equippedRunes = new ArrayList<>();
    private static final BigDecimal RUNES_IN_BUILD = BigDecimal.valueOf(6);
    private static final boolean BUILDS_ABOVE_MINIMUM_REQUIRED_VALUES = true;
    private static final boolean BUILDS_NOT_ABOVE_MINIMUM_REQUIRED_VALUES = false;

    public List<BuildSimulation> optimize() {
        equippedRunes.clear();

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

        var runeEfficiencies = runeEfficiencyService.
            findByMonsterSwarfarmId(monster.getSwarfarmId())
            .stream()
            .filter(runeEfficiency -> !equippedRunes.contains(runeEfficiency.getRune()))
            .toList();

        var limitedAttributePreferences = buildStrategy.getBuildPreferences()
            .stream()
            .filter(preference -> preference.getType().isLimited())
            .sorted(Comparator.comparing(buildPreference -> buildPreference.getAttribute().name()))
            .toList();

        var attributeEfficiencyBySlotByAttributeMap = attributeEfficiencyService.getAttributeEfficiencyBySlotByAttributeMap(limitedAttributePreferences, runeEfficiencies);

        return getBestBuildSimulation(buildStrategy, runeEfficiencies, baseMonster, attributeEfficiencyBySlotByAttributeMap);
    }

    private BuildSimulation getBestBuildSimulation(BuildStrategy buildStrategy, List<RuneEfficiency> runeEfficiencies, BaseMonster baseMonster,
                                                   Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var build = getBestBuild(buildStrategy, runeEfficiencies, attributeEfficiencyBySlotByAttributeMap);
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, build);
        equippedRunes.addAll(build.getRunes());

        return BuildSimulation.builder()
            .monsterName(baseMonster.getName())
            .monsterStats(monsterStats)
            .build(build)
            .build();
    }

    private Build getBestBuild(BuildStrategy buildStrategy, List<RuneEfficiency> runeEfficiencies,
                               Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var slotRuneEfficiencies = runeEfficiencies.stream().collect(Collectors.groupingBy(runeEfficiency -> runeEfficiency.getRune().getSlot()));
        return getMostEfficientRunesConsideringRuneSetBonuses(buildStrategy, slotRuneEfficiencies, attributeEfficiencyBySlotByAttributeMap);
    }

    private Build getMostEfficientRunesConsideringRuneSetBonuses(BuildStrategy buildStrategy, Map<Integer, List<RuneEfficiency>> slotRuneEfficiencies,
                                                                 Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var requiredSets = buildStrategy.getRuneSets();
        var slotsOccupied = requiredSets.stream().map(RuneSet::getRequirement).mapToInt(Integer::intValue).sum();
        var slotsFree = RUNES_IN_BUILD.intValue() - slotsOccupied;
        var viableSets = getViableUsefulSets(buildStrategy, slotsFree);

        var bestBuildOfEachRuneSetCombination = getPossibleRuneSetsCombinations(viableSets, requiredSets)
            .filter(simulationSets -> RUNES_IN_BUILD.intValue() >= simulationSets.stream().map(RuneSet::getRequirement).mapToInt(Integer::intValue).sum())
            .map(simulationSets -> {
                try {
                    return getMostEfficientRunesWithRequiredSetsAndAttributes(slotRuneEfficiencies, baseMonster, simulationSets, attributeEfficiencyBySlotByAttributeMap);
                } catch (NoPossibleBuildWithRequiredSetException exception) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .map(currentRunes -> currentRunes.values().stream().map(RuneEfficiency::getRune).toList())
            .map(currentRunes -> Build.builder().runes(currentRunes).efficiency(buildEfficiencyService.getBuildEfficiency(buildStrategy, currentRunes)).build())
            .collect(Collectors.groupingBy(build -> isAboveMinimumValueRequirements(build, buildStrategy)));

        if (bestBuildOfEachRuneSetCombination.containsKey(BUILDS_ABOVE_MINIMUM_REQUIRED_VALUES)) {
            return bestBuildOfEachRuneSetCombination.get(BUILDS_ABOVE_MINIMUM_REQUIRED_VALUES)
                .stream()
                .max(Comparator.comparing(Build::getEfficiency))
                .orElseThrow(NoPossibleBuildException::new);
        } else if (bestBuildOfEachRuneSetCombination.containsKey(BUILDS_NOT_ABOVE_MINIMUM_REQUIRED_VALUES)) {
            return bestBuildOfEachRuneSetCombination.get(BUILDS_NOT_ABOVE_MINIMUM_REQUIRED_VALUES)
                .stream()
                .max(Comparator.comparing(build -> howCloseToMinimumRequirements(build, buildStrategy)))
                .orElseThrow(NoPossibleBuildException::new);
        } else
            throw new NoPossibleBuildException();
    }

    private boolean isAboveMinimumValueRequirements(Build build, BuildStrategy buildStrategy) {
        var monsterStats = monsterBuildService.getMonsterStats(buildStrategy.getMonster().getBaseMonster(), build);
        return buildStrategy.getBuildPreferences()
            .stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
            .allMatch(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) >= buildPreference.getMinimumValue());
    }

    private BigDecimal howCloseToMinimumRequirements(Build build, BuildStrategy buildStrategy) {
        var monsterStats = monsterBuildService.getMonsterStats(buildStrategy.getMonster().getBaseMonster(), build);
        var minimumValuePreferences = buildStrategy.getBuildPreferences()
            .stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
            .toList();

        return minimumValuePreferences.stream()
            .map(buildPreference -> {
                var attributeValue = BigDecimal.valueOf(monsterStats.getAttributeValue(buildPreference.getAttribute()));
                var minimumValue = BigDecimal.valueOf(buildPreference.getMinimumValue());
                var maxEfficiency = BigDecimal.ONE;
                return attributeValue.divide(minimumValue, 4, RoundingMode.DOWN).min(maxEfficiency);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(minimumValuePreferences.size()), 4, RoundingMode.DOWN);
    }

    private Map<Integer, RuneEfficiency> getMostEfficientRunesWithRequiredSetsAndAttributes(Map<Integer, List<RuneEfficiency>> slotRuneEfficiencies, BaseMonster baseMonster, List<RuneSet> requiredRuneSets,
                                                                                            Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var mostEfficientRunes = getMostEfficientRunesWithoutPrerequisites(slotRuneEfficiencies);
        var mostEfficientRunesWithRequiredSets = runeSetOptimizerService.getMostEfficientRunesWithRequiredSets(mostEfficientRunes, slotRuneEfficiencies, requiredRuneSets);
        return getMostEfficientRunesWithinRequiredValues(mostEfficientRunesWithRequiredSets, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
    }

    private Stream<List<RuneSet>> getPossibleRuneSetsCombinations(List<RuneSet> viableSets, List<RuneSet> requiredSets) {
        var fourPieceSets = viableSets.stream().filter(runeSet -> runeSet.getRequirement() == 4).toList();
        var twoPieceSets = viableSets.stream().filter(runeSet -> runeSet.getRequirement() == 2).toList();

        var onlyRequiredRunes = Stream.of(requiredSets);

        var singleFourPieceCombinations = fourPieceSets.stream()
            .map(runeSet -> Stream.concat(Stream.of(runeSet), requiredSets.stream()).toList());

        var fourAndTwoPieceCombinations = fourPieceSets.stream()
            .map(fourPieceSet -> twoPieceSets.stream()
                .map(twoPieceSet -> Stream.concat(Stream.of(fourPieceSet, twoPieceSet), requiredSets.stream()).toList()).toList())
            .flatMap(Collection::stream);

        var singleTwoPieceCombinations = twoPieceSets.stream()
            .map(runeSet -> Stream.concat(Stream.of(runeSet), requiredSets.stream()).toList());

        var doubleTwoPieceCombinations = twoPieceSets.stream()
            .map(firstTwoPieceSet -> twoPieceSets.stream()
                .map(secondTwoPieceSet -> Stream.concat(Stream.of(firstTwoPieceSet, secondTwoPieceSet), requiredSets.stream()).toList()).toList())
            .flatMap(Collection::stream);

        var tripleTwoPieceCombinations = twoPieceSets.stream()
            .map(firstTwoPieceSet -> twoPieceSets.stream()
                .map(secondTwoPieceSet -> twoPieceSets.stream()
                    .map(thirdTwoPieceSet -> Stream.concat(Stream.of(firstTwoPieceSet, secondTwoPieceSet, thirdTwoPieceSet), requiredSets.stream()).toList()).toList()).toList())
            .flatMap(Collection::stream)
            .flatMap(Collection::stream);

        return Stream.of(onlyRequiredRunes, singleFourPieceCombinations, fourAndTwoPieceCombinations, singleTwoPieceCombinations, doubleTwoPieceCombinations, tripleTwoPieceCombinations).flatMap(Function.identity());
    }

    private List<RuneSet> getViableUsefulSets(BuildStrategy buildStrategy, Integer slotsFree) {
        var usefulAttributes = buildStrategy.getUsefulAttributes();

        return Stream.of(RuneSet.values())
            .filter(runeSet -> runeSet.getRequirement() <= slotsFree)
            .filter(runeSet -> usefulAttributes.contains(runeSet.getAttribute()))
            .collect(Collectors.toList());
    }

    private Map<Integer, RuneEfficiency> getMostEfficientRunesWithoutPrerequisites(Map<Integer, List<RuneEfficiency>> slotRuneEfficienciesMap) {
        return slotRuneEfficienciesMap.entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue()
                    .stream()
                    .max(Comparator.comparing(RuneEfficiency::getEfficiency))
                    .orElseThrow(SlotWithNoRunesException::new)));
    }

    private Map<Integer, RuneEfficiency> getMostEfficientRunesWithinRequiredValues(Map<Integer, RuneEfficiency> currentRunes, BaseMonster baseMonster, List<RuneSet> requiredRuneSets,
                                                                                   Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> attributeEfficiencyBySlotByAttributeMap) {
        var runes = currentRunes.values().stream().map(RuneEfficiency::getRune).toList();
        var buildEfficiency = currentRunes.values().stream().map(RuneEfficiency::getEfficiency).reduce(BigDecimal.ZERO, BigDecimal::add).divide(RUNES_IN_BUILD, 2, RoundingMode.DOWN);
        var build = Build.builder().runes(runes).efficiency(buildEfficiency).build();
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, build);
        var currentRuneSets = runes.stream().collect(Collectors.groupingBy(Rune::getSet));
        var limitedAttributePreferences = attributeEfficiencyBySlotByAttributeMap.keySet().stream().toList();

        var possibleUnmatchedPreferenceMinimumValue = limitedAttributePreferences.stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
            .filter(buildPreference -> buildPreference.getMinimumValue() > monsterStats.getAttributeValue(buildPreference.getAttribute()))
            .min(Comparator.comparing(buildPreference -> buildPreference.getMinimumValue() - monsterStats.getAttributeValue(buildPreference.getAttribute())));

        if (possibleUnmatchedPreferenceMinimumValue.isPresent()) {
            var unmatchedPreference = possibleUnmatchedPreferenceMinimumValue.get();
            try {
                var higherAttributeRune = getRuneWithHigherAttributeValue(unmatchedPreference, currentRunes, monsterStats, baseMonster, limitedAttributePreferences, attributeEfficiencyBySlotByAttributeMap.get(unmatchedPreference), currentRuneSets, requiredRuneSets);
                currentRunes.put(higherAttributeRune.getRune().getSlot(), higherAttributeRune);
                return getMostEfficientRunesWithinRequiredValues(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
            } catch (MinimumRequirementsNotAchievableException exception) {
                return currentRunes;
            }
        }

        var possibleUnmatchedPreferenceMaximumValue = limitedAttributePreferences.stream()
            .filter(buildPreference -> Objects.nonNull(buildPreference.getMaximumValue()))
            .filter(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) > buildPreference.getMaximumValue())
            .max(Comparator.comparing(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMaximumValue()));

        if (possibleUnmatchedPreferenceMaximumValue.isPresent()) {
            var unmatchedPreference = possibleUnmatchedPreferenceMaximumValue.get();
            try {
                var lowerAttributeRune = getRuneWithLowerAttributeValue(unmatchedPreference, currentRunes, monsterStats, baseMonster, limitedAttributePreferences, attributeEfficiencyBySlotByAttributeMap.get(unmatchedPreference), currentRuneSets, requiredRuneSets);
                currentRunes.put(lowerAttributeRune.getRune().getSlot(), lowerAttributeRune);
                return getMostEfficientRunesWithinRequiredValues(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
            } catch (MaximumRequirementsNotAchievableException exception) {
                return currentRunes;
            }
        }

        var biggestAttributeExcess = limitedAttributePreferences.stream()
            .filter(buildPreference -> buildPreference.getType().isLimited() && Objects.nonNull(buildPreference.getThresholdValue()))
            .filter(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) > buildPreference.getThresholdValue())
            .max(Comparator.comparing(buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getThresholdValue()));

        if (biggestAttributeExcess.isPresent()) {
            var preferenceWithAttributeExcess = biggestAttributeExcess.get();
            try {
                var lowerAttributeRune = getRuneWithLowerAttributeValue(preferenceWithAttributeExcess, currentRunes, monsterStats, baseMonster, limitedAttributePreferences, attributeEfficiencyBySlotByAttributeMap.get(preferenceWithAttributeExcess), currentRuneSets, requiredRuneSets);
                currentRunes.put(lowerAttributeRune.getRune().getSlot(), lowerAttributeRune);
                log.info("monsterStats={}", monsterStats);
                return getMostEfficientRunesWithinRequiredValues(currentRunes, baseMonster, requiredRuneSets, attributeEfficiencyBySlotByAttributeMap);
            } catch (RuntimeException exception) {
                return currentRunes;
            }
        }

        return currentRunes;
    }

    private RuneEfficiency getRuneWithHigherAttributeValue(BuildPreference unmatchedRequiredValuePreference, Map<Integer, RuneEfficiency> currentRunes, MonsterStats monsterStats, BaseMonster baseMonster,
                                                           List<BuildPreference> limitedAttributePreferences, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> slotEfficiencyMap,
                                                           Map<RuneSet, List<Rune>> currentRuneSets, List<RuneSet> requiredRuneSets) {
        var belowMinimumAttribute = unmatchedRequiredValuePreference.getAttribute();
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var runeSetsBonusesThaCanBeAdded = getRuneSetBonusesThatCanBeAdded(currentRuneSets, baseMonster);
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
                var minimumAttributeValues = limitedAttributePreferences.stream()
                    .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValueAboveLimit = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        var currentBonus = currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute());
                        return extraValueAboveLimit > 0 ? currentBonus.subtract(BigDecimal.valueOf(extraValueAboveLimit)) : currentBonus;
                    }));

                var slotMap = slotEfficiencyMap.get(slot);
                var higherAttributeBonus = getHigherAttributeWithoutMessingOthers(baseMonster, slotMap, currentRune, unmatchedRequiredValuePreference, minimumAttributeValues, runeSetsBonusesThaCanBeAdded, runeSetsBonusesThaCanBeLost, requiredRuneSetsThatCanBeLost);

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

        if (highestAttributeGainRatioSlot == 0) throw new MinimumRequirementsNotAchievableException();

        return highestAttributeGainRatioRune;
    }

    private RuneEfficiency getRuneWithLowerAttributeValue(BuildPreference unmatchedRequiredValuePreference, Map<Integer, RuneEfficiency> currentRunes, MonsterStats monsterStats, BaseMonster baseMonster,
                                                          List<BuildPreference> limitedAttributePreferences, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> slotEfficiencyMap,
                                                          Map<RuneSet, List<Rune>> currentRuneSets, List<RuneSet> requiredRuneSets) {
        var runeSetsBonusesThaCanBeLost = getRuneSetBonusesThatCanBeLost(currentRuneSets, baseMonster);
        var runeSetsBonusesThaCanBeAdded = getRuneSetBonusesThatCanBeAdded(currentRuneSets, baseMonster);
        var requiredRuneSetsThatCanBeLost = getRequiredRuneSetsThatCanBeLost(currentRunes, requiredRuneSets);

        var highestEfficiency = BigDecimal.ZERO;
        var highestEfficiencySlot = 0;
        var highestEfficiencyRune = new RuneEfficiency();

        for (var slot : slotEfficiencyMap.keySet()) {
            try {
                var currentRune = currentRunes.get(slot);
                var currentRuneEfficiency = currentRune.getEfficiency();

                var minimumAttributeValues = limitedAttributePreferences.stream()
                    .filter(buildPreference -> Objects.nonNull(buildPreference.getMinimumValue()))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValueAboveLimit = monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getMinimumValue();
                        var currentBonus = currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute());
                        return extraValueAboveLimit > 0 ? currentBonus.subtract(BigDecimal.valueOf(extraValueAboveLimit)) : currentBonus;
                    }));

                var thresholdAttributeValues = limitedAttributePreferences.stream()
                    .filter(buildPreference -> Objects.nonNull(buildPreference.getThresholdValue()))
                    .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> {
                        var extraValueBelowLimit = buildPreference.getThresholdValue() - monsterStats.getAttributeValue(buildPreference.getAttribute());
                        var currentBonus = currentRune.getLimitedAttributeBonusValue(buildPreference.getAttribute());
                        return extraValueBelowLimit > 0 ? currentBonus.add(BigDecimal.valueOf(extraValueBelowLimit)) : currentBonus;
                    }));

                var slotMap = slotEfficiencyMap.get(slot);

                var lowerAttributeBonus = getLowerAttributeWithoutMessingOthers(baseMonster, slotMap, currentRune, unmatchedRequiredValuePreference, minimumAttributeValues, thresholdAttributeValues, runeSetsBonusesThaCanBeLost, runeSetsBonusesThaCanBeAdded, requiredRuneSetsThatCanBeLost);

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

        if (highestEfficiencySlot == 0) throw new MaximumRequirementsNotAchievableException();

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

    private Map<RuneSet, BigDecimal> getRuneSetBonusesThatCanBeAdded(Map<RuneSet, List<Rune>> currentRuneSets, BaseMonster baseMonster) {
        return currentRuneSets.entrySet()
            .stream()
            .filter(entry -> entry.getKey().getBonusValue() > 0)
            .filter(entry -> entry.getKey().getRequirement() == entry.getValue().size() + 1)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getKey().calculateBonusEffect(baseMonster.getAttributeValue(entry.getKey().getAttribute()))
            ));
    }

    private BigDecimal getHigherAttributeWithoutMessingOthers(BaseMonster baseMonster, TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference unmatchedPreference,
                                                              Map<MonsterAttribute, BigDecimal> minimumAttributeValues, Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeAdded,
                                                              Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeLost, List<RuneSet> requiredRuneSetAboutToBeLost) {
        var currentAttribute = unmatchedPreference.getAttribute();
        var currentSet = currentRune.getRune().getSet();
        var canAddRuneSetBonus = !bonusRuneSetsAboutToBeAdded.containsKey(currentSet);
        var canLoseRuneSetBonus = bonusRuneSetsAboutToBeLost.containsKey(currentSet);
        var canLoseRequiredSet = requiredRuneSetAboutToBeLost.contains(currentSet);
        var higherAttributeValue = currentRune.getLimitedAttributeBonusValue(currentAttribute);
        var runeSetBonusBeingLost = new HashMap<MonsterAttribute, BigDecimal>();
        var runeSetBonusBeingAdded = new HashMap<MonsterAttribute, BigDecimal>();

        while (true) {
            var higherAttributeEntrySet = attributeEfficiencyMap.higherEntry(higherAttributeValue);

            if (higherAttributeEntrySet == null)
                throw new MoreEfficientRuneKeyNotFoundException();

            higherAttributeValue = higherAttributeEntrySet.getKey();
            var newRune = higherAttributeEntrySet.getValue();
            var newSet = newRune.getRune().getSet();

            if (canLoseRequiredSet && !currentSet.equals(newSet))
                continue;

            if (canLoseRuneSetBonus && !currentSet.equals(newSet)) {
                runeSetBonusBeingLost.put(currentSet.getAttribute(), bonusRuneSetsAboutToBeLost.get(currentSet));
                if (minimumAttributeValues.containsKey(currentSet.getAttribute())) {
                    var currentMinimumValue = minimumAttributeValues.get(currentSet.getAttribute());
                    var newMinimumValue = currentMinimumValue.add(bonusRuneSetsAboutToBeLost.get(currentSet));
                    minimumAttributeValues.put(currentSet.getAttribute(), newMinimumValue);
                }
            }

            if (canAddRuneSetBonus && bonusRuneSetsAboutToBeAdded.containsKey(newSet)) {
                runeSetBonusBeingAdded.put(newSet.getAttribute(), bonusRuneSetsAboutToBeAdded.get(newSet));
            }

            if (isAttributeGainedLessEfficientThanRequiredAttributesLost(currentRune, newRune, baseMonster, currentAttribute, runeSetBonusBeingAdded, runeSetBonusBeingLost, minimumAttributeValues)) {
                continue;
            }

            return higherAttributeValue;
        }
    }

    private boolean isAttributeGainedLessEfficientThanRequiredAttributesLost(RuneEfficiency currentRune, RuneEfficiency newRune, BaseMonster baseMonster, MonsterAttribute attributeBeingElevated, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingGained, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingLost, Map<MonsterAttribute, BigDecimal> minimumAttributeValues) {
        var limitedAttributes = minimumAttributeValues.keySet()
            .stream()
            .filter(monsterAttribute -> !monsterAttribute.equals(attributeBeingElevated))
            .toList();

        var attributeGainEfficiency = getAttributeIncreaseEfficiency(currentRune, newRune, baseMonster, BonusAttribute.valueOf(attributeBeingElevated.name()), runeSetBonusBeingGained);
        var attributeLossesEfficiency = limitedAttributes.stream()
            .map(MonsterAttribute::name)
            .map(BonusAttribute::valueOf)
            .map(attribute -> getAttributeDecreaseEfficiency(currentRune, newRune, baseMonster, attribute, runeSetBonusBeingLost))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return attributeLossesEfficiency.compareTo(attributeGainEfficiency) > 0;
    }

    private BigDecimal getLowerAttributeWithoutMessingOthers(BaseMonster baseMonster, TreeMap<BigDecimal, RuneEfficiency> attributeEfficiencyMap, RuneEfficiency currentRune, BuildPreference unmatchedPreference,
                                                             Map<MonsterAttribute, BigDecimal> minimumAttributeValues, Map<MonsterAttribute, BigDecimal> thresholdAttributeValues,
                                                             Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeLost, Map<RuneSet, BigDecimal> bonusRuneSetsAboutToBeAdded, List<RuneSet> requiredRuneSetAboutToBeLost) {
        var currentAttribute = unmatchedPreference.getAttribute();
        var currentSet = currentRune.getRune().getSet();
        var canLoseRuneSetBonus = bonusRuneSetsAboutToBeLost.containsKey(currentSet);
        var canAddRuneSetBonus = !bonusRuneSetsAboutToBeAdded.containsKey(currentSet);
        var canLoseRequiredSet = requiredRuneSetAboutToBeLost.contains(currentSet);
        var lowerAttribute = currentRune.getLimitedAttributeBonusValue(currentAttribute);
        var runeSetBonusBeingLost = new HashMap<MonsterAttribute, BigDecimal>();
        var runeSetBonusBeingAdded = new HashMap<MonsterAttribute, BigDecimal>();

        while (true) {
            var lowerAttributeEntrySet = attributeEfficiencyMap.lowerEntry(lowerAttribute);

            if (lowerAttributeEntrySet == null)
                throw new MoreEfficientRuneKeyNotFoundException();

            var newRune = lowerAttributeEntrySet.getValue();
            var newSet = newRune.getRune().getSet();
            var newSetAttribute = newSet.getAttribute();
            lowerAttribute = lowerAttributeEntrySet.getKey();

            if (canLoseRequiredSet && !currentSet.equals(newSet))
                continue;

            if (canLoseRuneSetBonus && !currentSet.equals(newSet)) {
                runeSetBonusBeingLost.put(currentSet.getAttribute(), bonusRuneSetsAboutToBeLost.get(currentSet));

                if (minimumAttributeValues.containsKey(currentSet.getAttribute())) {
                    var currentMinimumValue = minimumAttributeValues.get(currentSet.getAttribute());
                    var newMinimumValue = currentMinimumValue.add(bonusRuneSetsAboutToBeLost.get(currentSet));
                    minimumAttributeValues.put(currentSet.getAttribute(), newMinimumValue);
                }

                if (thresholdAttributeValues.containsKey(currentSet.getAttribute())) {
                    var currentThresholdValue = thresholdAttributeValues.get(currentSet.getAttribute());
                    var newThresholdValue = currentThresholdValue.subtract(bonusRuneSetsAboutToBeLost.get(currentSet));
                    thresholdAttributeValues.put(currentSet.getAttribute(), newThresholdValue);
                }
            }

            if (canAddRuneSetBonus && bonusRuneSetsAboutToBeAdded.containsKey(newSet)) {
                runeSetBonusBeingAdded.put(newSetAttribute, bonusRuneSetsAboutToBeAdded.get(newSet));

                if (minimumAttributeValues.containsKey(newSetAttribute)) {
                    var currentMinimumValue = minimumAttributeValues.get(newSetAttribute);
                    var newMinimumValue = currentMinimumValue.add(bonusRuneSetsAboutToBeLost.get(newSet));
                    minimumAttributeValues.put(newSetAttribute, newMinimumValue);
                }

                if (thresholdAttributeValues.containsKey(newSetAttribute)) {
                    var currentThresholdValue = thresholdAttributeValues.get(newSetAttribute);
                    var newThresholdValue = currentThresholdValue.subtract(bonusRuneSetsAboutToBeLost.get(newSet));
                    thresholdAttributeValues.put(newSet.getAttribute(), newThresholdValue);
                }
            }

            var attributesGoingBelowMinimumValue = getAttributesGoingBelowMinimumValues(minimumAttributeValues, newRune);
            if (!attributesGoingBelowMinimumValue.isEmpty()) {
                continue;
            }

            if (isExcessCreatedHigherThanExcessReduced(currentRune, newRune, baseMonster, currentAttribute, runeSetBonusBeingAdded, runeSetBonusBeingLost, thresholdAttributeValues)) {
                continue;
            }

            return lowerAttribute;
        }
    }

    private List<MonsterAttribute> getAttributesGoingBelowMinimumValues(Map<MonsterAttribute, BigDecimal> minimumAttributeValues, RuneEfficiency runeEfficiency) {
        return minimumAttributeValues.entrySet()
            .stream()
            .filter(entrySet -> {
                var monsterAttribute = entrySet.getKey();
                var minimumValue = entrySet.getValue();
                var actualValue = runeEfficiency.getLimitedAttributeBonusValue(monsterAttribute);
                return actualValue.compareTo(minimumValue) < 0;
            })
            .map(Map.Entry::getKey)
            .toList();
    }

    private boolean isExcessCreatedHigherThanExcessReduced(RuneEfficiency currentRune, RuneEfficiency newRune, BaseMonster baseMonster, MonsterAttribute attributeBeingElevated, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingAdded, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingLost, Map<MonsterAttribute, BigDecimal> thresholdAttributeValues) {
        var requiredAttributes = thresholdAttributeValues.keySet()
            .stream()
            .filter(monsterAttribute -> !monsterAttribute.equals(attributeBeingElevated))
            .toList();

        var excessBeingReduced = getAttributeDecreaseEfficiency(currentRune, newRune, baseMonster, BonusAttribute.valueOf(attributeBeingElevated.name()), runeSetBonusBeingLost);
        var excessBeingAdded = requiredAttributes.stream()
            .map(MonsterAttribute::name)
            .map(BonusAttribute::valueOf)
            .map(attribute -> getAttributeIncreaseEfficiency(currentRune, newRune, baseMonster, attribute, runeSetBonusBeingAdded))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return excessBeingAdded.compareTo(excessBeingReduced) > 0;
    }

    private BigDecimal getAttributeIncreaseEfficiency(RuneEfficiency currentRune, RuneEfficiency newRune, BaseMonster baseMonster, BonusAttribute bonusAttribute, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingAdded) {
        var monsterAttribute = bonusAttribute.getMonsterAttribute();
        var currentValue = currentRune.getLimitedAttributeBonusValue(monsterAttribute);
        var runeSetBonusIncrease = runeSetBonusBeingAdded.getOrDefault(bonusAttribute.getMonsterAttribute(), BigDecimal.ZERO);
        var newValue = newRune.getLimitedAttributeBonusValue(monsterAttribute).add(runeSetBonusIncrease);
        var attributeLoss = newValue.subtract(currentValue);
        var fullyMaxedSubStatBonus = bonusAttribute.getEffectAggregationType().calculate(baseMonster.getAttributeValue(monsterAttribute), bonusAttribute.getFullyMaxedSubStatBonus().intValue());
        return attributeLoss.divide(fullyMaxedSubStatBonus, 4, RoundingMode.DOWN);
    }

    private BigDecimal getAttributeDecreaseEfficiency(RuneEfficiency currentRune, RuneEfficiency newRune, BaseMonster baseMonster, BonusAttribute bonusAttribute, Map<MonsterAttribute, BigDecimal> runeSetBonusBeingLost) {
        var monsterAttribute = bonusAttribute.getMonsterAttribute();
        var currentValue = currentRune.getLimitedAttributeBonusValue(monsterAttribute);
        var runeSetBonusDecrease = runeSetBonusBeingLost.getOrDefault(bonusAttribute.getMonsterAttribute(), BigDecimal.ZERO);
        var newValue = newRune.getLimitedAttributeBonusValue(monsterAttribute).subtract(runeSetBonusDecrease);
        var attributeLoss = currentValue.subtract(newValue);
        var fullyMaxedSubStatBonus = bonusAttribute.getEffectAggregationType().calculate(baseMonster.getAttributeValue(monsterAttribute), bonusAttribute.getFullyMaxedSubStatBonus().intValue());
        return attributeLoss.divide(fullyMaxedSubStatBonus, 4, RoundingMode.DOWN);
    }

}
