package com.optmizer.summonerwaroptimizer.model.rune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "stat_value")
    private Integer value;

    private BonusAttribute bonusAttribute;

    @JsonIgnore
    public BigDecimal getBonusAttributeValue(Integer baseAttributeValue) {
        return bonusAttribute.getEffectAggregationType().calculate(baseAttributeValue, this.value);
    }


}
