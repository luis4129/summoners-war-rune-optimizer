package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityEfficiency {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @ToString.Exclude
    private RuneEfficiency runeEfficiency;

    private Integer priority;
    private List<MonsterAttribute> attributes;
    private BigDecimal efficiency;
}
