package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildSimulation {

    private String monsterName;
    private MonsterStats monsterStats;
    private Build build;
}
