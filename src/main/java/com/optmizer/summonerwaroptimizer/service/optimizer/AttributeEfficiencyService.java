package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttributeEfficiencyService {

    public Map<BuildPreference, Map<Integer, TreeMap<BigDecimal, RuneEfficiency>>> getAttributeEfficiencyBySlotByAttributeMap(List<BuildPreference> limitedAttributePreferences, List<RuneEfficiency> runeEfficiencies) {
        return limitedAttributePreferences.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                attribute -> getAttributeEfficiencyBySlotMap(attribute, runeEfficiencies)));
    }

    private Map<Integer, TreeMap<BigDecimal, RuneEfficiency>> getAttributeEfficiencyBySlotMap(BuildPreference buildPreference, List<RuneEfficiency> runeEfficiencies) {
        return runeEfficiencies.stream()
            .collect(Collectors.groupingBy(runeEfficiency -> runeEfficiency.getRune().getSlot()))
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entrySet -> getAttributeEfficiencyTreeMap(buildPreference, entrySet.getValue())));
    }

    private TreeMap<BigDecimal, RuneEfficiency> getAttributeEfficiencyTreeMap(BuildPreference buildPreference, List<RuneEfficiency> runeEfficiencies) {
        var monsterAttribute = buildPreference.getAttribute();

        return runeEfficiencies.stream().collect(Collectors.toMap(
            runeEfficiency -> runeEfficiency.getLimitedAttributeBonusValue(monsterAttribute),
            Function.identity(),
            this::getTheMostEfficientRune,
            TreeMap::new));
    }

    private RuneEfficiency getTheMostEfficientRune(RuneEfficiency rune1, RuneEfficiency rune2) {
        return rune1.getEfficiency().compareTo(rune2.getEfficiency()) > 0 ? rune1 : rune2;
    }
}
