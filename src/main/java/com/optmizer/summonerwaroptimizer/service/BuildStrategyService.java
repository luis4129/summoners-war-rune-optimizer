package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.build.BuildStrategy;
import com.optmizer.summonerwaroptimizer.repository.BuildStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BuildStrategyService {

    @Autowired
    private BuildStrategyRepository buildStrategyRepository;

    public List<BuildStrategy> findAll() {
        return buildStrategyRepository.findAll();
    }

    public void saveAll(List<BuildStrategy> buildStrategies) {
        buildStrategyRepository.saveAll(buildStrategies);
    }


}
