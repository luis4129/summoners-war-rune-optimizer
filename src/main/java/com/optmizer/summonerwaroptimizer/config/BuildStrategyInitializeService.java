package com.optmizer.summonerwaroptimizer.config;

import com.optmizer.summonerwaroptimizer.model.build.BuildStrategy;
import com.optmizer.summonerwaroptimizer.service.BuildStrategyService;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BuildStrategyInitializeService {

    @Autowired
    private BuildStrategyService buildStrategyService;

    @Autowired
    private MonsterService monsterService;

    @Autowired
    private YamlConfig yamlConfig;

    private static final Long VERDEHILE_ID = 7400575165L;

    public void initializeBuildStrategies() {
        var buildStrategies = toBuildStrategies(yamlConfig.getBuildStrategies());

        buildStrategyService.saveAll(buildStrategies);
        log.info("c=BuildStrategyInitializeService m=initializeBuildStrategies message=Build strategies have been initialized");
    }

    private List<BuildStrategy> toBuildStrategies(List<YamlBuildStrategy> configs) {
        return configs.stream()
            .map(this::toBuildStrategy)
            .collect(Collectors.toList());
    }

    private BuildStrategy toBuildStrategy(YamlBuildStrategy config) {
        var buildPreferences = config.getBuildPreferences();

        var buildStrategy = BuildStrategy.builder()
            .monster(monsterService.findBySwarfarmId(config.getMonster()))
            .runeSets(config.getRuneSets())
            .buildPreferences(buildPreferences)
            .build();

        buildPreferences.forEach(buildPreference -> buildPreference.setBuildStrategy(buildStrategy));

        return buildStrategy;
    }
}