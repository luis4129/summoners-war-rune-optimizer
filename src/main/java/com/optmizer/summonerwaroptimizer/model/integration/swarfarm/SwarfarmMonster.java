package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwarfarmMonster {

    @JsonProperty("unit_id")
    private Long id;

    @JsonProperty("unit_master_id")
    private Long masterId;

    @JsonProperty("unit_level")
    private Integer level;

    @JsonProperty("class")
    private Integer grade;

    @JsonProperty("runes")
    private List<SwarfarmRune> runes;

}
