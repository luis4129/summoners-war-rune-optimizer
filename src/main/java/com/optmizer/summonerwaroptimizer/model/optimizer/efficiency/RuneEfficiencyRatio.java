package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuneEfficiencyRatio {


    private BigDecimal runeEfficiencyRatio;
    private BigDecimal maxEfficiencyRatio;

    @Singular
    private List<LimitedAttributeBonus> limitedAttributeBonuses;

}
