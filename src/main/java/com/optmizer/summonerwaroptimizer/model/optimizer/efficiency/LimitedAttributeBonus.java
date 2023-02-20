package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitedAttributeBonus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @ToString.Exclude
    private RuneEfficiency runeEfficiency;

    private MonsterAttribute monsterAttribute;
    private BigDecimal bonus;
}
