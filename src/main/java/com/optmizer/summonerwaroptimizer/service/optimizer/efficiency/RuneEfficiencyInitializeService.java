package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RuneEfficiencyInitializeService {

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    @Autowired
    private BuildStrategyService buildStrategyService;

    @Autowired
    private RuneService runeService;

    public void initializeRuneEfficiencies() {
        var buildStrategies = buildStrategyService.findAll();
        var runes = runeService.findAll();

        for (BuildStrategy buildStrategy : buildStrategies) {
            for (Rune rune : runes) {
                var runeEfficiency = runeEfficiencyService.getRuneEfficiency(buildStrategy, rune, true);
                runeEfficiencyService.save(runeEfficiency);
            }
        }
        log.info("c=RuneEfficiencyInitializeService m=initializeRuneEfficiencies message=Complete");
    }

}
