package com.optmizer.summonerwaroptimizer.model.optimizer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BuildPreferenceType {

    AS_HIGH_AS_POSSIBLE(false),
    WITHIN_REQUIRED_RANGE(true),
    ONLY_REQUIRED_VALUE(true);

    private boolean isLimited;

}
