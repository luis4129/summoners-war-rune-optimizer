package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants;

import com.optmizer.summonerwaroptimizer.model.rune.Attribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;

import java.util.Map;

import static java.util.Map.entry;

public class SwarfarmConstants {

    private SwarfarmConstants() {
    }

    public static final String ACCOUNT_DATA_FOLDER = "src\\main\\resources\\swarfarm\\account";
    public static final String BESTIARY_DATA_FILE = "src\\main\\resources\\swarfarm\\bestiary_data.json";
    public static final Integer ATTRIBUTE_INDEX = 0;
    public static final Integer VALUE_INDEX = 1;
    public static final Integer GRIND_VALUE_INDEX = 2;
    public static final Integer IS_ENCHANTED_INDEX = 3;
    public static final Integer EMPTY_STAT = 0;
    public static final Map<Integer, Attribute> SWARFARM_ATTRIBUTE_CONVERSION_MAP = buildAttributeConversionMap();
    public static final Map<Integer, RuneSet> SWARFARM_RUNE_SET_CONVERSION_MAP = buildRuneSetConversionMap();

    private static Map<Integer, Attribute> buildAttributeConversionMap() {
        return Map.ofEntries(
            entry(1, Attribute.FLAT_HIT_POINTS),
            entry(2, Attribute.HIT_POINTS),
            entry(3, Attribute.FLAT_ATTACK),
            entry(4, Attribute.ATTACK),
            entry(5, Attribute.FLAT_DEFENSE),
            entry(6, Attribute.DEFENSE),
            entry(8, Attribute.SPEED),
            entry(9, Attribute.CRITICAl_RATE),
            entry(10, Attribute.CRITICAL_DAMAGE),
            entry(11, Attribute.RESISTANCE),
            entry(12, Attribute.ACCURACY));
    }

    private static Map<Integer, RuneSet> buildRuneSetConversionMap() {
        return Map.ofEntries(
            entry(1, RuneSet.ENERGY),
            entry(2, RuneSet.GUARD),
            entry(3, RuneSet.SWIFT),
            entry(4, RuneSet.BLADE),
            entry(5, RuneSet.RAGE),
            entry(6, RuneSet.FOCUS),
            entry(7, RuneSet.ENDURE),
            entry(8, RuneSet.FATAL),
            entry(10, RuneSet.DESPAIR),
            entry(11, RuneSet.VAMPIRE),
            entry(13, RuneSet.VIOLENT),
            entry(14, RuneSet.NEMESIS),
            entry(15, RuneSet.WILL),
            entry(16, RuneSet.SHIELD),
            entry(17, RuneSet.REVENGE),
            entry(18, RuneSet.DESTROY),
            entry(19, RuneSet.FIGHT),
            entry(20, RuneSet.DETERMINATION),
            entry(21, RuneSet.ENHANCE),
            entry(22, RuneSet.ACCURACY),
            entry(23, RuneSet.TOLERANCE));
    }
}
