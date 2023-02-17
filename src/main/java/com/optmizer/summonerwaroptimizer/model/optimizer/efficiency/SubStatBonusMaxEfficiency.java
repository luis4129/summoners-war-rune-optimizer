package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubStatBonusMaxEfficiency {

    private BonusAttribute attribute;
    private BigDecimal ratio;
    private BigDecimal fullRatio;

    public BonusMaxEfficiency toBonusMaxEfficiency() {
        return BonusMaxEfficiency.builder()
            .attribute(attribute)
            .ratio(fullRatio)
            .build();
    }

}
