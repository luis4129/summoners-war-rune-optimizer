package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.exception.UnmappedRuneSetConversionException;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class RuneSetConversionServiceTest {

    @InjectMocks
    private RuneSetConversionService runeSetConversionService;

    private static final Integer SWARFARM_ENERGY_INDEX = 1;
    private static final Integer SWARFARM_GUARD_INDEX = 2;
    private static final Integer SWARFARM_SWIFT_INDEX = 3;
    private static final Integer SWARFARM_BLADE_INDEX = 4;
    private static final Integer SWARFARM_RAGE_INDEX = 5;
    private static final Integer SWARFARM_FOCUS_INDEX = 6;
    private static final Integer SWARFARM_ENDURE_INDEX = 7;
    private static final Integer SWARFARM_FATAL_INDEX = 8;
    private static final Integer SWARFARM_DESPAIR_INDEX = 10;
    private static final Integer SWARFARM_VAMPIRE_INDEX = 11;
    private static final Integer SWARFARM_VIOLENT_INDEX = 13;
    private static final Integer SWARFARM_NEMESIS_INDEX = 14;
    private static final Integer SWARFARM_WILL_INDEX = 15;
    private static final Integer SWARFARM_SHIELD_INDEX = 16;
    private static final Integer SWARFARM_REVENGE_INDEX = 17;
    private static final Integer SWARFARM_DESTROY_INDEX = 18;
    private static final Integer SWARFARM_FIGHT_INDEX = 19;
    private static final Integer SWARFARM_DETERMINATION_INDEX = 20;
    private static final Integer SWARFARM_ENHANCE_INDEX = 21;
    private static final Integer SWARFARM_ACCURACY_INDEX = 22;
    private static final Integer SWARFARM_TOLERANCE_INDEX = 23;
    private static final Integer SWARFARM_UNMAPPED_INDEX = 9;

    @Test
    void shouldReturnEnergyRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_ENERGY_INDEX);

        //then
        assertEquals(RuneSet.ENERGY, runeSet);
    }

    @Test
    void shouldReturnGuardRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_GUARD_INDEX);

        //then
        assertEquals(RuneSet.GUARD, runeSet);
    }

    @Test
    void shouldReturnSwiftRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_SWIFT_INDEX);

        //then
        assertEquals(RuneSet.SWIFT, runeSet);
    }

    @Test
    void shouldReturnBladeRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_BLADE_INDEX);

        //then
        assertEquals(RuneSet.BLADE, runeSet);
    }

    @Test
    void shouldReturnRageRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_RAGE_INDEX);

        //then
        assertEquals(RuneSet.RAGE, runeSet);
    }

    @Test
    void shouldReturnFocusRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_FOCUS_INDEX);

        //then
        assertEquals(RuneSet.FOCUS, runeSet);
    }

    @Test
    void shouldReturnEndureRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_ENDURE_INDEX);

        //then
        assertEquals(RuneSet.ENDURE, runeSet);
    }

    @Test
    void shouldReturnFatalRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_FATAL_INDEX);

        //then
        assertEquals(RuneSet.FATAL, runeSet);
    }

    @Test
    void shouldReturnDespairRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_DESPAIR_INDEX);

        //then
        assertEquals(RuneSet.DESPAIR, runeSet);
    }

    @Test
    void shouldReturnVampireRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_VAMPIRE_INDEX);

        //then
        assertEquals(RuneSet.VAMPIRE, runeSet);
    }

    @Test
    void shouldReturnViolentRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_VIOLENT_INDEX);

        //then
        assertEquals(RuneSet.VIOLENT, runeSet);
    }

    @Test
    void shouldReturnNemesisRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_NEMESIS_INDEX);

        //then
        assertEquals(RuneSet.NEMESIS, runeSet);
    }

    @Test
    void shouldReturnWillRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_WILL_INDEX);

        //then
        assertEquals(RuneSet.WILL, runeSet);
    }

    @Test
    void shouldReturnShieldRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_SHIELD_INDEX);

        //then
        assertEquals(RuneSet.SHIELD, runeSet);
    }

    @Test
    void shouldReturnRevengeRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_REVENGE_INDEX);

        //then
        assertEquals(RuneSet.REVENGE, runeSet);
    }

    @Test
    void shouldReturnDestroyRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_DESTROY_INDEX);

        //then
        assertEquals(RuneSet.DESTROY, runeSet);
    }

    @Test
    void shouldReturnFightRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_FIGHT_INDEX);

        //then
        assertEquals(RuneSet.FIGHT, runeSet);
    }

    @Test
    void shouldReturnDeterminationRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_DETERMINATION_INDEX);

        //then
        assertEquals(RuneSet.DETERMINATION, runeSet);
    }

    @Test
    void shouldReturnEnhanceRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_ENHANCE_INDEX);

        //then
        assertEquals(RuneSet.ENHANCE, runeSet);
    }

    @Test
    void shouldReturnAccuracyRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_ACCURACY_INDEX);

        //then
        assertEquals(RuneSet.ACCURACY, runeSet);
    }

    @Test
    void shouldReturnToleranceRuneSet() {
        //when
        var runeSet = runeSetConversionService.toRuneSet(SWARFARM_TOLERANCE_INDEX);

        //then
        assertEquals(RuneSet.TOLERANCE, runeSet);
    }

    @Test
    void shouldThrowExceptionWhenIndexIsUnmapped() {
        //when
        assertThrows(UnmappedRuneSetConversionException.class, () -> runeSetConversionService.toRuneSet(SWARFARM_UNMAPPED_INDEX));
    }
}