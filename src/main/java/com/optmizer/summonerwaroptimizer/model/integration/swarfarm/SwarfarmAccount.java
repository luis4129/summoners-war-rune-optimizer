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
public class SwarfarmAccount {

    @JsonProperty("unit_list")
    private List<SwarfarmMonster> monsters;

    @JsonProperty("runes")
    private List<SwarfarmRune> unequippedRunes;


}
