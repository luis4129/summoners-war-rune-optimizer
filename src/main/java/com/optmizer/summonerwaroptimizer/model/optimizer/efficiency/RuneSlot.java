package com.optmizer.summonerwaroptimizer.model.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public enum RuneSlot {

    NONE,

    ONE(List.of(BonusAttribute.FLAT_ATTACK),
        List.of(BonusAttribute.FLAT_DEFENSE, BonusAttribute.DEFENSE)),

    TWO(BonusAttribute.SPEED,
        BonusAttribute.HIT_POINTS,
        BonusAttribute.ATTACK,
        BonusAttribute.DEFENSE,
        BonusAttribute.FLAT_HIT_POINTS,
        BonusAttribute.FLAT_ATTACK,
        BonusAttribute.FLAT_DEFENSE),

    THREE(List.of(BonusAttribute.FLAT_DEFENSE),
        List.of(BonusAttribute.FLAT_ATTACK, BonusAttribute.ATTACK)),

    FOUR(BonusAttribute.CRITICAL_DAMAGE,
        BonusAttribute.CRITICAL_RATE,
        BonusAttribute.HIT_POINTS,
        BonusAttribute.ATTACK,
        BonusAttribute.DEFENSE,
        BonusAttribute.FLAT_HIT_POINTS,
        BonusAttribute.FLAT_ATTACK,
        BonusAttribute.FLAT_DEFENSE),

    FIVE(BonusAttribute.FLAT_HIT_POINTS),

    SIX(BonusAttribute.RESISTANCE,
        BonusAttribute.ACCURACY,
        BonusAttribute.HIT_POINTS,
        BonusAttribute.ATTACK,
        BonusAttribute.DEFENSE,
        BonusAttribute.FLAT_HIT_POINTS,
        BonusAttribute.FLAT_ATTACK,
        BonusAttribute.FLAT_DEFENSE);

    private final List<BonusAttribute> mainStatOptions;
    private final List<BonusAttribute> invalidSubStatOptions;

    RuneSlot(BonusAttribute... mainStatOptions) {
        this.mainStatOptions = List.of(mainStatOptions);
        this.invalidSubStatOptions = Collections.emptyList();
    }

    RuneSlot(List<BonusAttribute> mainStatOptions, List<BonusAttribute> invalidSubStatOptions) {
        this.mainStatOptions = mainStatOptions;
        this.invalidSubStatOptions = invalidSubStatOptions;
    }

}
