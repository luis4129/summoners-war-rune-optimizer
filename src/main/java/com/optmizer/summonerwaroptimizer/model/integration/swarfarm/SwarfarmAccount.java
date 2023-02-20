package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwarfarmAccount {

    @JsonProperty("unit_list")
    private List<SwarfarmMonster> monsters;

    @JsonProperty("runes")
    private List<SwarfarmRune> unequippedRunes;


}
