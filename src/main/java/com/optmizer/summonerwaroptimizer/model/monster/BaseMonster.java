package com.optmizer.summonerwaroptimizer.model.monster;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseMonster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonValue
    private String name;

    private Long swarfarmId;
    private String imageFileName;

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
