package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.BuildFactory;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RuneSetBonusServiceTest {

    @InjectMocks
    private RuneSetBonusService runeSetBonusService;

    private static final MonsterAttribute CRITICAL_RATE_ATTRIBUTE = MonsterAttribute.CRITICAL_RATE;
    private static final MonsterAttribute ATTACK_ATTRIBUTE = MonsterAttribute.ATTACK;
    private static final Integer BASE_CRITICAL_RATE_VALUE = 15;
    private static final Integer BASE_ATTACK_VALUE = 600;
    private static final BigDecimal CRITICAL_RATE_BONUS = BigDecimal.valueOf(12);
    private static final BigDecimal ATTACK_BONUS = BigDecimal.valueOf(210).setScale(2, RoundingMode.UP);

    @Test
    void shouldCalculateRuneSetBonusWithSumAggregator() {
        //given
        var build = BuildFactory.getBuildWithSet(RuneSet.BLADE);

        //when
        var runeSetBonus = runeSetBonusService.getRuneSetBonus(CRITICAL_RATE_ATTRIBUTE, BASE_CRITICAL_RATE_VALUE, build);

        //then
        assertEquals(CRITICAL_RATE_BONUS, runeSetBonus);
    }

    @Test
    void shouldCalculateRuneSetBonusWithMultiplyAggregator() {
        //given
        var build = BuildFactory.getBuildWithSet(RuneSet.FATAL);

        //when
        var runeSetBonus = runeSetBonusService.getRuneSetBonus(ATTACK_ATTRIBUTE, BASE_ATTACK_VALUE, build);

        //then
        assertEquals(ATTACK_BONUS, runeSetBonus);
    }
}