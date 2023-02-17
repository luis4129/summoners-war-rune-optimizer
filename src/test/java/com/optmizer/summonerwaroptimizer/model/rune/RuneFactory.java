package com.optmizer.summonerwaroptimizer.model.rune;

import java.util.List;

public class RuneFactory {

    public static final RuneSet SET = RuneSet.VIOLENT;
    public static final Integer SLOT = 2;
    public static final Integer GRADE = 6;
    public static final Integer LEVEL = 15;


    public static List<Rune> getValidRunes() {
        return List.of(
            Rune.builder()
                .slot(1)
                .set(RuneSet.BLADE)
                .grade(6)
                .level(12)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.FLAT_ATTACK).value(118).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(15).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.ATTACK).value(9).grindValue(6).enchanted(true).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_DAMAGE).value(14).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.SPEED).value(6).build()
                ))
                .build(),
            Rune.builder()
                .slot(2)
                .set(RuneSet.REVENGE)
                .grade(6)
                .level(15)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.ATTACK).value(63).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(16).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.HIT_POINTS).value(7).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.ACCURACY).value(8).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.FLAT_HIT_POINTS).value(282).build()
                ))
                .build(),
            Rune.builder()
                .slot(3)
                .set(RuneSet.DESPAIR)
                .grade(6)
                .level(9)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.FLAT_DEFENSE).value(94).build())
                .prefixStat(PrefixStat.builder().bonusAttribute(BonusAttribute.CRITICAL_DAMAGE).value(7).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.HIT_POINTS).value(7).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(16).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.SPEED).value(10).build()
                ))
                .build(),
            Rune.builder()
                .slot(4)
                .set(RuneSet.SWIFT)
                .grade(6)
                .level(15)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.CRITICAL_DAMAGE).value(80).build())
                .prefixStat(PrefixStat.builder().bonusAttribute(BonusAttribute.FLAT_HIT_POINTS).value(322).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.HIT_POINTS).value(5).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(16).grindValue(6).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.FLAT_DEFENSE).value(11).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.ATTACK).value(6).grindValue(3).build()
                ))
                .build(),
            Rune.builder()
                .slot(5)
                .set(RuneSet.BLADE)
                .grade(6)
                .level(12)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.FLAT_HIT_POINTS).value(1800).build())
                .prefixStat(PrefixStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(4).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_DAMAGE).value(19).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.HIT_POINTS).value(8).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.ATTACK).value(12).grindValue(5).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.FLAT_DEFENSE).value(20).build()
                ))
                .build(),
            Rune.builder()
                .slot(6)
                .set(RuneSet.SWIFT)
                .grade(6)
                .level(15)
                .mainStat(MainStat.builder().bonusAttribute(BonusAttribute.ATTACK).value(63).build())
                .subStats(List.of(
                    SubStat.builder().bonusAttribute(BonusAttribute.FLAT_ATTACK).value(15).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.SPEED).value(9).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_RATE).value(14).enchanted(true).build(),
                    SubStat.builder().bonusAttribute(BonusAttribute.CRITICAL_DAMAGE).value(6).build()
                ))
                .build()
        );
    }


    public static List<Rune> getRunesWithSet(RuneSet runeSet) {
        return List.of(
            getValidRuneFromSet(runeSet),
            getValidRuneFromSet(runeSet),
            getValidRuneFromSet(runeSet),
            getValidRuneFromSet(runeSet),
            getValidRuneFromSet(runeSet),
            getValidRuneFromSet(runeSet)
        );
    }

    public static Rune getValidRuneFromSet(RuneSet runeSet) {
        return Rune.builder()
            .set(runeSet)
            .slot(SLOT)
            .grade(GRADE)
            .level(LEVEL)
            .mainStat(MainStatFactory.getValidMainStat())
            .prefixStat(PrefixStatFactory.getValidPrefixStat())
            .subStats(SubStatFactory.getValidSubStats())
            .build();
    }

    public static List<Rune> getRunesWithSpeedAttribute() {
        return List.of(Rune.builder()
            .set(SET)
            .slot(SLOT)
            .grade(GRADE)
            .level(LEVEL)
            .mainStat(MainStatFactory.getMainStatWithSpeedAttribute())
            .prefixStat(PrefixStatFactory.getPrefixStatWithSpeedAttribute())
            .subStats(SubStatFactory.getPrefixStatWithSpeedAttribute())
            .build());

    }

    public static List<Rune> getRunesWithAttackAttribute() {
        return List.of(Rune.builder()
            .set(SET)
            .slot(SLOT)
            .grade(GRADE)
            .level(LEVEL)
            .mainStat(MainStatFactory.getMainStatWithAttackAttribute())
            .prefixStat(PrefixStatFactory.getPrefixStatWithAttackAttribute())
            .subStats(SubStatFactory.getPrefixStatWithAttackAttribute())
            .build());

    }
}
