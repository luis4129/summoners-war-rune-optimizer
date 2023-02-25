package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.LimitedAttributeBonus;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.repository.RuneEfficiencyRepository;
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
public class RuneEfficiencyService {

    @Autowired
    private RuneEfficiencyRepository runeEfficiencyRepository;

    @Autowired
    private MainStatEfficiencyService mainStatEfficiencyService;

    @Autowired
    private PrefixStatEfficiencyService prefixStatEfficiencyService;

    @Autowired
    private SubStatEfficiencyService subStatEfficiencyService;

    @Autowired
    private RuneSetEfficiencyService runeSetEfficiencyService;

    public void save(RuneEfficiency runeEfficiency) {
        runeEfficiencyRepository.save(runeEfficiency);
    }

    public List<RuneEfficiency> findByMonsterSwarfarmId(Long swarfarmId) {
        return runeEfficiencyRepository.findByBuildStrategy_Monster_SwarfarmId_OrderByEfficiencyDesc(swarfarmId);
    }

    public List<RuneEfficiency> findByRuneSwarfarmId(Long swarfarmId) {
        return Collections.emptyList();
        //return runeEfficiencyRepository.findByRuneSwarfarmIdOrderByEfficiencyDesc(swarfarmId);
        //List<RuneEfficiency> findByRuneSwarfarmIdOrderByEfficiencyDesc(Long swarfarmId);
    }

    public RuneEfficiency getRuneEfficiency(BuildStrategy buildStrategy, Rune rune, boolean considerOnlyUnlimitedAttributes) {
        var runeEfficiencyRatio = getRuneEfficiencyRatio(buildStrategy, rune, considerOnlyUnlimitedAttributes);
        var maxEfficiencyRatio = getMaxEfficiencyRatio(buildStrategy, rune, considerOnlyUnlimitedAttributes);

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

    public BigDecimal getRuneEfficiencyValue(BigDecimal runeEfficiencyRatio, BigDecimal maxEfficiencyRatio) {
        if (maxEfficiencyRatio.equals(BigDecimal.ZERO))
            return BigDecimal.ZERO;

        return runeEfficiencyRatio
            .divide(maxEfficiencyRatio, 4, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100));
    }


    private BigDecimal getRuneEfficiencyRatio(BuildStrategy buildStrategy, Rune rune, boolean considerOnlyUnlimitedAttributes) {
        var efficiencyAttributes = considerOnlyUnlimitedAttributes ? buildStrategy.getUnlimitedAttributes() : buildStrategy.getUsefulAttributes();
        var requiredRuneSets = buildStrategy.getRuneSets();
        var runeSetEfficiencyRatio = runeSetEfficiencyService.getRuneSetEfficiencyRatio(rune.getSet(), requiredRuneSets, efficiencyAttributes);

        return getRuneEfficiencyRatioWithoutRuneSet(buildStrategy, rune, considerOnlyUnlimitedAttributes).add(runeSetEfficiencyRatio);
    }


    public BigDecimal getRuneEfficiencyRatioWithoutRuneSet(BuildStrategy buildStrategy, Rune rune, boolean considerOnlyUnlimitedAttributes) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var efficiencyAttributes = considerOnlyUnlimitedAttributes ? buildStrategy.getUsefulAttributesBonusBonus() : buildStrategy.getUsefulAttributesBonus();

        var mainStatEfficiencyRatio = mainStatEfficiencyService.getMainStatEfficiencyRatio(baseMonster, rune.getMainStat().getBonusAttribute(), rune.getGrade(), efficiencyAttributes);
        var subStatsEfficiencyRatio = subStatEfficiencyService.getSubStatEfficiencyRatio(baseMonster, rune.getSubStats(), efficiencyAttributes);
        var prefixStatEfficiencyRatio = prefixStatEfficiencyService.getPrefixStatEfficiencyRatio(rune.getPrefixStat(), efficiencyAttributes);

        return mainStatEfficiencyRatio.add(subStatsEfficiencyRatio).add(prefixStatEfficiencyRatio);
    }

    private BigDecimal getMaxEfficiencyRatio(BuildStrategy buildStrategy, Rune rune, boolean considerOnlyUnlimitedAttributes) {
        var efficiencyAttributes = considerOnlyUnlimitedAttributes ? buildStrategy.getUnlimitedAttributes() : buildStrategy.getUsefulAttributes();
        var maxRuneSetEfficiencyRatio = runeSetEfficiencyService.getMaxRuneSetEfficiency(rune.getSet().getRequirement(), buildStrategy.getRuneSets(), efficiencyAttributes);

        return getMaxEfficiencyRatioWithoutRuneSet(buildStrategy, rune, considerOnlyUnlimitedAttributes).add(maxRuneSetEfficiencyRatio);
    }

    public BigDecimal getMaxEfficiencyRatioWithoutRuneSet(BuildStrategy buildStrategy, Rune rune, boolean considerOnlyUnlimitedAttributes) {
        var efficiencyAttributes = considerOnlyUnlimitedAttributes ? buildStrategy.getUsefulAttributesBonusBonus() : buildStrategy.getUsefulAttributesBonus();
        var slot = rune.getSlot();
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var remainingAttributes = new ArrayList<>(efficiencyAttributes);

        var maxMainStatEfficiencyRatio = mainStatEfficiencyService.getMaxMainStatFromSlot(baseMonster, slot, remainingAttributes);
        var maxSubStatsEfficiencyRatio = subStatEfficiencyService.getMaxEfficiencyRatio(baseMonster, slot, remainingAttributes);
        var maxPrefixStatEfficiencyRatio = prefixStatEfficiencyService.getMaxPrefixStatEfficiencyRatio(remainingAttributes);

        return maxMainStatEfficiencyRatio.add(maxSubStatsEfficiencyRatio).add(maxPrefixStatEfficiencyRatio);
    }

    private List<LimitedAttributeBonus> getLimitedAttributeBonuses(BuildStrategy buildStrategy, Rune rune) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var limitedAttributes = buildStrategy.getLimitedAttributes();

        var maxMainStatEfficiencyRatio = mainStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getMainStat().getBonusAttribute(), rune.getGrade(), limitedAttributes);
        var maxSubStatsEfficiencyRatio = subStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getSubStats(), limitedAttributes);
        var maxPrefixStatEfficiencyRatio = prefixStatEfficiencyService.getLimitedAttributeBonuses(baseMonster, rune.getPrefixStat(), limitedAttributes);

        return Stream.of(maxMainStatEfficiencyRatio, maxSubStatsEfficiencyRatio, maxPrefixStatEfficiencyRatio)
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
