package com.optmizer.summonerwaroptimizer.model.build;

import com.optmizer.summonerwaroptimizer.model.rune.RuneFactory;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;

public class BuildFactory {

    public static final Long ID = 1L;
    public static final Integer LEVEL = 40;
    public static final Integer GRADE = 6;

    public static Build getValidBuild() {
        return Build.builder()
            .runes(RuneFactory.getValidRunes())
            .build();
    }

    public static Build getBuildWithSet(RuneSet runeSet) {
        return Build.builder()
            .runes(RuneFactory.getRunesWithSet(runeSet))
            .build();
    }

    public static Build getBuildWithSpeedAttribute() {
        return Build.builder()
            .runes(RuneFactory.getRunesWithSpeedAttribute())
            .build();
    }

    public static Build getBuildWithAttackAttribute() {
        return Build.builder()
            .runes(RuneFactory.getRunesWithAttackAttribute())
            .build();
    }

}
