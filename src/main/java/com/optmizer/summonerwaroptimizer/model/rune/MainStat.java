package com.optmizer.summonerwaroptimizer.model.rune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.MainStatMaxBonus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainStat {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @OneToOne
    @JsonIgnore
    @ToString.Exclude
    private Rune rune;

    @Column(name = "stat_value")
    private Integer value;

    private BonusAttribute bonusAttribute;

    @JsonIgnore
    public BigDecimal getBonusAttributeValue(Integer baseAttributeValue) {
        var fullyLeveledBonus = getFullyLeveledGradeBonus(bonusAttribute, rune.getGrade());
        return bonusAttribute.getEffectAggregationType().calculate(baseAttributeValue, fullyLeveledBonus);
    }

    private Integer getFullyLeveledGradeBonus(BonusAttribute bonusAttribute, Integer grade) {
        return MainStatMaxBonus.valueOf(bonusAttribute.name())
            .getMaxGradeBonusList()
            .get(grade)
            .intValue();
    }


}
