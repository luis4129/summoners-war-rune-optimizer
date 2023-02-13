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
public class SwarfarmRune {

    @JsonProperty("rune_id")
    private Long id;

    @JsonProperty("set_id")
    private Integer set;

    @JsonProperty("slot_no")
    private Integer slot;

    @JsonProperty("class")
    private Integer grade;

    @JsonProperty("upgrade_curr")
    private Integer level;

    @JsonProperty("pri_eff")
    private List<Integer> primaryEffect;

    @JsonProperty("prefix_eff")
    private List<Integer> prefixEffect;

    @JsonProperty("sec_eff")
    private List<List<Integer>> secondaryEffects;

}
