package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildSimulation;
import com.optmizer.summonerwaroptimizer.service.optimizer.OptimizerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("optimize")
public class OptimizerResource {

    @Autowired
    private OptimizerService optimizerService;

    @GetMapping
    public List<BuildSimulation> optimize() {
        return optimizerService.optimize();
    }

}
