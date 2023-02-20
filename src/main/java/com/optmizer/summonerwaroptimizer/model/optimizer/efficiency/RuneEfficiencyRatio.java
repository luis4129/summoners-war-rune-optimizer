package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuneEfficiencyRatio {

    @Singular
    private List<PriorityEfficiencyRatio> priorityEfficiencyRatios;

    @Singular
    private List<LimitedAttributeBonus> limitedAttributeBonuses;

}
