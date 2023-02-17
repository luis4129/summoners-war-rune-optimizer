package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private BuildStrategy buildStrategy;

    private MonsterAttribute attribute;
    private Integer minimumValue;


}
