package com.optmizer.summonerwaroptimizer.model.rune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubStat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Rune rune;

    @Column(name = "stat_value")
    private Integer value;

    private BonusAttribute bonusAttribute;
    private Integer grindValue;
    private boolean enchanted;

    @JsonIgnore
    public BigDecimal getBonusAttributeValue(Integer baseAttributeValue) {
        return bonusAttribute.getEffectAggregationType().calculate(baseAttributeValue, getTotalValue());
    }

    @JsonIgnore
    public Integer getTotalValue() {
        return value + grindValue;
    }

    @JsonIgnore
    public BigDecimal getFullyGrindedValue() {
        var maxGrindValue = rune.isAncient() ? bonusAttribute.getMaxAncientGrind() : bonusAttribute.getMaxGrindValue();

        return BigDecimal.valueOf(value).add(maxGrindValue);
    }

}
