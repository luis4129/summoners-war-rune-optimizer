package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuneEfficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Rune rune;

    @ManyToOne
    @JsonIgnore
    private BuildStrategy buildStrategy;

    private BigDecimal efficiency;

    @ToString.Exclude
    @OneToMany(mappedBy = "runeEfficiency", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<LimitedAttributeBonus> limitedAttributeBonuses;

    @JsonIgnore
    public BigDecimal getLimitedAttributeBonusValue(MonsterAttribute monsterAttribute) {
        return limitedAttributeBonuses.stream()
            .filter(limitedAttributeBonus -> limitedAttributeBonus.getMonsterAttribute().equals(monsterAttribute))
            .map(LimitedAttributeBonus::getBonus)
            .findFirst()
            .orElse(BigDecimal.ZERO);
    }

    @JsonProperty("monster")
    public String getMonsterName() {
        return buildStrategy.getMonsterName();
    }
    
}
