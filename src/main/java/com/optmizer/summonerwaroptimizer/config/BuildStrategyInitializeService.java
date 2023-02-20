package com.optmizer.summonerwaroptimizer.config;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreferenceType;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import com.optmizer.summonerwaroptimizer.service.optimizer.BuildStrategyService;
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
            .monster(monsterService.findBySwarmFarmId(config.getMonster()))
            .priority(config.getPriority())
            .runeSets(config.getSets())
            .buildPreferences(buildPreferences)
            .build();

        buildPreferences.forEach(buildPreference -> buildPreference.setBuildStrategy(buildStrategy));
        buildPreferences.stream().filter(buildPreference -> buildPreference.getType().equals(BuildPreferenceType.AS_HIGH_AS_POSSIBLE)).forEach(buildPreference -> buildPreference.setPriority(0));

        return buildStrategy;
    }
}
