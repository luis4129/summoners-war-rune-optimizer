package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.exception.NoPossibleBuildWithRequiredSetException;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RuneSetOptimizerService {

    public Map<Integer, RuneEfficiency> getMostEfficientRunesWithRequiredSets(Map<Integer, RuneEfficiency> currentRunes, Map<Integer, List<RuneEfficiency>> slotRuneEfficienciesMap, List<RuneSet> requiredRuneSets) {
        var possibleRuneSetMissingRunes = getRuneSetMissingRunes(currentRunes, requiredRuneSets);
        if (possibleRuneSetMissingRunes.isPresent()) {
            var runeSetMissingRunes = possibleRuneSetMissingRunes.get();

            var slotMissingRequiredSet = getMostEfficientSlotMissingRequiredSet(currentRunes, slotRuneEfficienciesMap, requiredRuneSets, runeSetMissingRunes);
            if (slotMissingRequiredSet.isPresent()) {
                var newRuneSlot = slotMissingRequiredSet.get();
                var newRune = getMostEfficientRuneReplacementForMissingRuneSet(slotRuneEfficienciesMap.get(newRuneSlot), runeSetMissingRunes);
                currentRunes.put(newRuneSlot, newRune);
                return getMostEfficientRunesWithRequiredSets(currentRunes, slotRuneEfficienciesMap, requiredRuneSets);
            }

            var newRuneSlot = getMostEfficientSlotToReplaceExtraSetWithMissingSet(currentRunes, slotRuneEfficienciesMap, requiredRuneSets, runeSetMissingRunes);
            var newRune = getMostEfficientRuneReplacementForMissingRuneSet(slotRuneEfficienciesMap.get(newRuneSlot), runeSetMissingRunes);
            currentRunes.put(newRuneSlot, newRune);
            return getMostEfficientRunesWithRequiredSets(currentRunes, slotRuneEfficienciesMap, requiredRuneSets);
        }
        return currentRunes;
    }

    private Optional<RuneSet> getRuneSetMissingRunes(Map<Integer, RuneEfficiency> currentRunes, List<RuneSet> requiredRuneSets) {
        return requiredRuneSets.stream()
            .collect(Collectors.groupingBy(Function.identity()))
            .entrySet()
            .stream()
            .filter(entry -> {
                var runeSet = entry.getKey();
                var requirement = entry.getKey().getRequirement() * entry.getValue().size();
                var runesFromThatSet = Math.toIntExact(currentRunes.values().stream().map(RuneEfficiency::getRune).map(Rune::getSet).filter(runeSet::equals).count());
                return requirement > runesFromThatSet;
            })
            .min(Comparator.comparing(entry -> entry.getKey().getAttribute().name()))
            .map(Map.Entry::getKey);
    }

    private Integer getMostEfficientSlotToReplaceExtraSetWithMissingSet(Map<Integer, RuneEfficiency> currentRunes, Map<Integer, List<RuneEfficiency>> slotRuneEfficienciesMap, List<RuneSet> requiredRuneSets, RuneSet runeSetMissingRunes) {
        var runeSetsWithExtraRunes = getRuneSetsWithExtraRunes(requiredRuneSets, currentRunes);

        return currentRunes.values()
            .stream()
            .filter(runeEfficiency -> !runeEfficiency.getRune().getSet().equals(runeSetMissingRunes))
            .filter(runeEfficiency -> runeSetsWithExtraRunes.contains(runeEfficiency.getRune().getSet()))
            .min(Comparator.comparing(replaceableRune -> replaceableRune.getEfficiency()
                .subtract(slotRuneEfficienciesMap.get(replaceableRune.getRune().getSlot())
                    .stream()
                    .filter(runeEfficiency -> runeSetMissingRunes.equals(runeEfficiency.getRune().getSet()))
                    .max(Comparator.comparing(RuneEfficiency::getEfficiency))
                    .map(RuneEfficiency::getEfficiency)
                    .orElse(BigDecimal.ZERO))))
            .map(RuneEfficiency::getRune)
            .map(Rune::getSlot)
            .orElseThrow(NoPossibleBuildWithRequiredSetException::new);
    }

    private Optional<Integer> getMostEfficientSlotMissingRequiredSet(Map<Integer, RuneEfficiency> currentRunes, Map<Integer, List<RuneEfficiency>> slotRuneEfficienciesMap, List<RuneSet> requiredRuneSets, RuneSet runeSetMissingRunes) {
        return currentRunes.values()
            .stream()
            .filter(runeEfficiency -> !requiredRuneSets.contains(runeEfficiency.getRune().getSet()))
            .min(Comparator.comparing(replaceableRune -> replaceableRune.getEfficiency()
                .subtract(slotRuneEfficienciesMap.get(replaceableRune.getRune().getSlot())
                    .stream()
                    .filter(runeEfficiency -> runeSetMissingRunes.equals(runeEfficiency.getRune().getSet()))
                    .max(Comparator.comparing(RuneEfficiency::getEfficiency))
                    .map(RuneEfficiency::getEfficiency)
                    .orElse(BigDecimal.ZERO))))
            .map(RuneEfficiency::getRune)
            .map(Rune::getSlot);
    }

    private RuneEfficiency getMostEfficientRuneReplacementForMissingRuneSet(List<RuneEfficiency> slotRuneEfficienciesMap, RuneSet runeSetMissingRunes) {
        return slotRuneEfficienciesMap.stream()
            .filter(runeEfficiency -> runeSetMissingRunes.equals(runeEfficiency.getRune().getSet()))
            .max(Comparator.comparing(RuneEfficiency::getEfficiency))
            .orElseThrow(NoPossibleBuildWithRequiredSetException::new);
    }

    private List<RuneSet> getRuneSetsWithExtraRunes(List<RuneSet> requiredRuneSets, Map<Integer, RuneEfficiency> currentRunes) {
        var requiredRuneSetsCountMap = requiredRuneSets.stream().collect(Collectors.groupingBy(Function.identity()));

        return currentRunes.values()
            .stream()
            .map(RuneEfficiency::getRune)
            .map(Rune::getSet)
            .collect(Collectors.groupingBy(Function.identity()))
            .entrySet()
            .stream()
            .filter(entry -> {
                var runeSet = entry.getKey();
                var runesCount = entry.getValue().size();
                var runesRequired = runeSet.getRequirement() * requiredRuneSetsCountMap.get(runeSet).size();
                return runesCount > runesRequired;
            })
            .map(Map.Entry::getKey)
            .toList();
    }
}
