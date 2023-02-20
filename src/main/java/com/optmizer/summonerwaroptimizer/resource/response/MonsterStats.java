package com.optmizer.summonerwaroptimizer.resource.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
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

    @JsonIgnore
    public Integer getAttributeValue(MonsterAttribute monsterAttribute) {
        return switch (monsterAttribute) {
            case NONE -> null;
            case HIT_POINTS -> hitPoints;
            case ATTACK -> attack;
            case DEFENSE -> defense;
            case SPEED -> speed;
            case CRITICAL_RATE -> criticalRate;
            case CRITICAL_DAMAGE -> criticalDamage;
            case RESISTANCE -> resistance;
            case ACCURACY -> accuracy;
        };
    }

}
