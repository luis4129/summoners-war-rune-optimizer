package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.service.simulation.MonsterBuildService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.RuneSetBonusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BuildEfficiencyService {

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    @Autowired
    private RuneSetEfficiencyService runeSetEfficiencyService;

    @Autowired
    private RuneSetBonusService runeSetBonusService;

    @Autowired
    private MonsterBuildService monsterBuildService;

    private static final Integer FOUR_PIECE_SET = 4;
    private static final Integer TWO_PIECE_SET = 2;

    public BigDecimal getBuildEfficiency(BuildStrategy buildStrategy, List<Rune> runes) {
        var buildEfficiencyRatio = getBuildEfficiencyRatio(buildStrategy, runes);
        var buildMaxEfficiencyRatio = getBuildMaxEfficiencyRatio(buildStrategy, runes);

        if (buildMaxEfficiencyRatio.equals(BigDecimal.ZERO))
            return BigDecimal.ZERO;

        return buildEfficiencyRatio
            .divide(buildMaxEfficiencyRatio, 4, RoundingMode.DOWN)
            .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal getBuildEfficiencyRatio(BuildStrategy buildStrategy, List<Rune> runes) {
        var equippedRuneSetsCountMap = runeSetBonusService.getEquippedRuneSetsCountMap(runes);

        var runesEfficiency = runes.stream()
            .map(rune -> runeEfficiencyService.getRuneEfficiencyRatioWithoutRuneSet(buildStrategy, rune, false))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var runeSetEfficiency = getRuneSetEfficiencyRatio(buildStrategy, equippedRuneSetsCountMap);
        var runeExcessValueRatio = getExcessEfficiencyRatio(buildStrategy, runes);

        return runesEfficiency.add(runeSetEfficiency).subtract(runeExcessValueRatio);
    }

    private BigDecimal getBuildMaxEfficiencyRatio(BuildStrategy buildStrategy, List<Rune> runes) {
        var maxRunesEfficiencyRatio = runes.stream()
            .map(rune -> runeEfficiencyService.getMaxEfficiencyRatioWithoutRuneSet(buildStrategy, rune, false))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var maxRuneSetEfficiencyRatio = getRuneSetMaxEfficiencyRatio(buildStrategy);

        return maxRunesEfficiencyRatio.add(maxRuneSetEfficiencyRatio);
    }

    private BigDecimal getRuneSetEfficiencyRatio(BuildStrategy buildStrategy, Map<RuneSet, BigDecimal> equippedRuneSetsCountMap) {
        return equippedRuneSetsCountMap.entrySet().stream().map(entry -> {
                var equippedRuneSet = entry.getKey();
                var requiredSets = buildStrategy.getRuneSets();
                var usefulAttributes = buildStrategy.getUsefulAttributes();
                var effectMultiplier = entry.getValue();
                return runeSetEfficiencyService.getRuneSetEfficiencyRatio(equippedRuneSet, requiredSets, usefulAttributes).multiply(effectMultiplier);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getRuneSetMaxEfficiencyRatio(BuildStrategy buildStrategy) {
        var requiredSets = buildStrategy.getRuneSets();
        var usefulAttributes = buildStrategy.getUsefulAttributes();

        var fourPieceRatio = runeSetEfficiencyService.getMaxRuneSetEfficiency(FOUR_PIECE_SET, requiredSets, usefulAttributes);
        var twoPieceRatio = runeSetEfficiencyService.getMaxRuneSetEfficiency(TWO_PIECE_SET, requiredSets, usefulAttributes);

        return fourPieceRatio.add(twoPieceRatio);
    }

    private BigDecimal getExcessEfficiencyRatio(BuildStrategy buildStrategy, List<Rune> runes) {
        var baseMonster = buildStrategy.getMonster().getBaseMonster();
        var monsterStats = monsterBuildService.getMonsterStats(baseMonster, Build.builder().runes(runes).build());

        var attributesExtraValues = buildStrategy.getBuildPreferences()
            .stream()
            .filter(buildPreference -> buildPreference.getType().isLimited())
            .filter(buildPreference -> Objects.nonNull(buildPreference.getThresholdValue()))
            .collect(Collectors.toMap(BuildPreference::getAttribute, buildPreference -> monsterStats.getAttributeValue(buildPreference.getAttribute()) - buildPreference.getThresholdValue()));

        return attributesExtraValues.entrySet()
            .stream()
            .map(entry -> getExcessValueEfficiencyRatio(entry.getKey(), entry.getValue()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getExcessValueEfficiencyRatio(MonsterAttribute monsterAttribute, Integer excessValue) {
        var bonusAttribute = BonusAttribute.valueOf(monsterAttribute.name());
        return BigDecimal.valueOf(excessValue).divide(bonusAttribute.getFullyMaxedSubStatBonus(), 4, RoundingMode.DOWN);
    }

}
