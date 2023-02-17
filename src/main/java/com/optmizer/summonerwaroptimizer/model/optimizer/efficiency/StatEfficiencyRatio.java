package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatEfficiencyRatio {

    private BigDecimal efficiencyRatio;
    private BigDecimal maxEfficiencyRatio;

}
