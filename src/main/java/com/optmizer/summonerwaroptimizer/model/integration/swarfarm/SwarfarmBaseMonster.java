package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwarfarmBaseMonster {

    @JsonProperty("com2us_id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("image_filename")
    private String imageFileName;

    @JsonProperty("max_lvl_hp")
    private Integer hitPoints;

    @JsonProperty("max_lvl_attack")
    private Integer attack;

    @JsonProperty("max_lvl_defense")
    private Integer defense;

    @JsonProperty("speed")
    private Integer speed;

    @JsonProperty("crit_rate")
    private Integer criticalRate;

    @JsonProperty("crit_damage")
    private Integer criticalDamage;

    @JsonProperty("resistance")
    private Integer resistance;

    @JsonProperty("accuracy")
    private Integer accuracy;

}
