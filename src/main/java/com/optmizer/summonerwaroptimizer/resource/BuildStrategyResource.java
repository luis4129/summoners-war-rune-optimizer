package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("strategies")
public class BuildStrategyResource {

    @Autowired
    private BuildStrategyService buildStrategyService;

    @GetMapping
    public List<BuildStrategy> findAll() {
        return buildStrategyService.findAll();
    }

}
