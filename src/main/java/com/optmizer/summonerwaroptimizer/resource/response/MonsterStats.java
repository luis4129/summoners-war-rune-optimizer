package com.optmizer.summonerwaroptimizer.resource.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonsterStats implements Serializable {

    private Integer hitPoints;
    private Integer attack;
    private Integer defense;
    private Integer speed;
    private Integer criticalRate;
    private Integer criticalDamage;
    private Integer resistance;
    private Integer accuracy;

}
