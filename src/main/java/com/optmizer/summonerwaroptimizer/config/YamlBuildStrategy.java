package com.optmizer.summonerwaroptimizer.config;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import lombok.Data;

import java.util.List;

@Data
public class YamlBuildStrategy {

    private Long monster;
    private List<RuneSet> runeSets;
    private List<BuildPreference> buildPreferences;
}
