package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.repository.RuneEfficiencyRepository;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OptimizerService {

    @Autowired
    private RuneEfficiencyRepository runeEfficiencyRepository;

    @Autowired
    private BuildStrategyService buildStrategyService;

    @Autowired
    private RuneService runeService;

    public void getEfficiency() {
        for (BuildStrategy buildStrategy : buildStrategyService.findAll()) {
            for (Rune rune : runeService.findAll()) {
                
            }
        }
    }


}
