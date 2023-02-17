package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.optimizer.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

                var runeEfficiency = RuneEfficiency.builder()
                    .rune(rune)
                    .buildStrategy(buildStrategy)
                    .efficiency(getEfficiencyValue(buildStrategy, rune))
                    .build();

                runeEfficiencyService.save(runeEfficiency);
            }
        }
        log.info("c=RuneEfficiencyInitializeService m=initializeRuneEfficiencies message=Complete");
    }

    private BigDecimal getEfficiencyValue(BuildStrategy buildStrategy, Rune rune) {
        var usefulAttributes = buildStrategy.getUsefulAttributesBonus();
        var baseMonster = buildStrategy.getMonster().getBaseMonster();

        var mainStatEfficiencyRatio = mainStatEfficiencyService.getSubStatEfficiencyRatio(buildStrategy, rune, usefulAttributes, baseMonster);
        var subStatsEfficiencyRatio = subStatEfficiencyService.getSubStatEfficiencyRatio(buildStrategy, rune, usefulAttributes, baseMonster);
        var prefixStatEfficiencyRatio = prefixStatEfficiencyService.getSubStatEfficiencyRatioValue(buildStrategy, rune.getPrefixStat(), usefulAttributes, baseMonster);
        var runeSetEfficiencyRatio = runeSetEfficiencyService.getSubStatEfficiencyRatio(buildStrategy, rune.getSet());

        var bonusEfficiencyRatio = mainStatEfficiencyRatio.getEfficiencyRatio()
            .add(subStatsEfficiencyRatio.getEfficiencyRatio())
            .add(prefixStatEfficiencyRatio.getEfficiencyRatio())
            .add(runeSetEfficiencyRatio.getEfficiencyRatio());

        var maxBonusEfficiencyRatio = mainStatEfficiencyRatio.getMaxEfficiencyRatio()
            .add(subStatsEfficiencyRatio.getMaxEfficiencyRatio())
            .add(prefixStatEfficiencyRatio.getMaxEfficiencyRatio())
            .add(runeSetEfficiencyRatio.getMaxEfficiencyRatio());

        if (BigDecimal.ZERO.equals(maxBonusEfficiencyRatio))
            return BigDecimal.ZERO;

        return bonusEfficiencyRatio.divide(maxBonusEfficiencyRatio, 3, RoundingMode.DOWN);
    }

}
