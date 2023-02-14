package com.optmizer.summonerwaroptimizer.service.simulation;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterFactory;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.MainStatBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.PrefixStatBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.RuneSetBonusService;
import com.optmizer.summonerwaroptimizer.service.simulation.bonus.SubStatBonusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonsterBuildServiceTest {

    @Mock
    private RuneSetBonusService runeSetBonusService;

    @Mock
    private MainStatBonusService mainStatBonusService;

    @Mock
    private PrefixStatBonusService prefixStatBonusService;

    @Mock
    private SubStatBonusService subStatBonusService;

    @InjectMocks
    private MonsterBuildService monsterBuildService;

    private static final Integer EXPECTED_HP = 14958;
    private static final Integer EXPECTED_ATK = 2300;
    private static final Integer EXPECTED_DEF = 630;
    private static final Integer EXPECTED_SPD = 129;
    private static final Integer EXPECTED_CR = 100;
    private static final Integer EXPECTED_CD = 175;
    private static final Integer EXPECTED_RES = 15;
    private static final Integer EXPECTED_ACC = 8;

    private static final BigDecimal NO_BONUS = BigDecimal.ZERO;
    private static final BigDecimal BLADE_SET_BONUS = BigDecimal.valueOf(12);

    private static final BigDecimal MAIN_STAT_ATTACK_BONUS = BigDecimal.valueOf(1141.12);

    private static final BigDecimal MAIN_STAT_CRITICAL_DAMAGE_BONUS = BigDecimal.valueOf(80);

    private static final BigDecimal MAIN_STAT_HIT_POINTS_BONUS = BigDecimal.valueOf(1800);

    private static final BigDecimal MAIN_STAT_DEFENSE_BONUS = BigDecimal.valueOf(94);

    private static final BigDecimal PREFIX_STAT_CRITICAL_DAMAGE_BONUS = BigDecimal.valueOf(7);

    private static final BigDecimal PREFIX_STAT_HIT_POINTS_BONUS = BigDecimal.valueOf(322);

    private static final BigDecimal PREFIX_STAT_CRITICAL_RATE_BONUS = BigDecimal.valueOf(4);

    private static final BigDecimal SUB_STATS_HIT_POINTS_BONUS = BigDecimal.valueOf(2950.95);
    private static final BigDecimal SUB_STATS_ATTACK_BONUS = BigDecimal.valueOf(345.92);
    private static final BigDecimal SUB_STATS_DEFENSE_BONUS = BigDecimal.valueOf(31);
    private static final BigDecimal SUB_STATS_SPEED_BONUS = BigDecimal.valueOf(30);
    private static final BigDecimal SUB_STATS_CRITICAL_RATE_BONUS = BigDecimal.valueOf(69);
    private static final BigDecimal SUB_STATS_CRITICAL_DAMAGE_BONUS = BigDecimal.valueOf(38);
    private static final BigDecimal SUB_STATS_ACCURACY_BONUS = BigDecimal.valueOf(8);

    @Test
    void shouldDelegateStatsBonusMathToSpecificClassesThenSumAndRoundTheResults() {
        //given
        var monster = MonsterFactory.getValidMonster();
        var baseMonster = monster.getBaseMonster();
        var build = monster.getBuild();

        when(runeSetBonusService.getRuneSetBonus(any(), any(), any()))
            .thenReturn(NO_BONUS);
        when(runeSetBonusService.getRuneSetBonus(MonsterAttribute.CRITICAL_RATE, baseMonster.getCriticalRate(), build))
            .thenReturn(BLADE_SET_BONUS);

        when(mainStatBonusService.getMainStatBonus(any(), any(), any()))
            .thenReturn(NO_BONUS);
        when(mainStatBonusService.getMainStatBonus(MonsterAttribute.ATTACK, baseMonster.getAttack(), build))
            .thenReturn(MAIN_STAT_ATTACK_BONUS);
        when(mainStatBonusService.getMainStatBonus(MonsterAttribute.CRITICAL_DAMAGE, baseMonster.getCriticalDamage(), build))
            .thenReturn(MAIN_STAT_CRITICAL_DAMAGE_BONUS);
        when(mainStatBonusService.getMainStatBonus(MonsterAttribute.HIT_POINTS, baseMonster.getHitPoints(), build))
            .thenReturn(MAIN_STAT_HIT_POINTS_BONUS);
        when(mainStatBonusService.getMainStatBonus(MonsterAttribute.DEFENSE, baseMonster.getDefense(), build))
            .thenReturn(MAIN_STAT_DEFENSE_BONUS);


        when(prefixStatBonusService.getPrefixStatBonus(any(), any(), any()))
            .thenReturn(NO_BONUS);
        when(prefixStatBonusService.getPrefixStatBonus(MonsterAttribute.CRITICAL_DAMAGE, baseMonster.getCriticalDamage(), build))
            .thenReturn(PREFIX_STAT_CRITICAL_DAMAGE_BONUS);
        when(prefixStatBonusService.getPrefixStatBonus(MonsterAttribute.CRITICAL_RATE, baseMonster.getCriticalRate(), build))
            .thenReturn(PREFIX_STAT_CRITICAL_RATE_BONUS);
        when(prefixStatBonusService.getPrefixStatBonus(MonsterAttribute.HIT_POINTS, baseMonster.getHitPoints(), build))
            .thenReturn(PREFIX_STAT_HIT_POINTS_BONUS);

        when(subStatBonusService.getSubStatsBonus(any(), any(), any()))
            .thenReturn(NO_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.HIT_POINTS, baseMonster.getHitPoints(), build))
            .thenReturn(SUB_STATS_HIT_POINTS_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.ATTACK, baseMonster.getAttack(), build))
            .thenReturn(SUB_STATS_ATTACK_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.DEFENSE, baseMonster.getDefense(), build))
            .thenReturn(SUB_STATS_DEFENSE_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.SPEED, baseMonster.getSpeed(), build))
            .thenReturn(SUB_STATS_SPEED_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.CRITICAL_RATE, baseMonster.getCriticalRate(), build))
            .thenReturn(SUB_STATS_CRITICAL_RATE_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.CRITICAL_DAMAGE, baseMonster.getCriticalDamage(), build))
            .thenReturn(SUB_STATS_CRITICAL_DAMAGE_BONUS);
        when(subStatBonusService.getSubStatsBonus(MonsterAttribute.ACCURACY, baseMonster.getAccuracy(), build))
            .thenReturn(SUB_STATS_ACCURACY_BONUS);

        //when
        var monsterStats = monsterBuildService.getMonsterStats(monster);

        //then
        assertEquals(EXPECTED_HP, monsterStats.getHitPoints());
        assertEquals(EXPECTED_ATK, monsterStats.getAttack());
        assertEquals(EXPECTED_DEF, monsterStats.getDefense());
        assertEquals(EXPECTED_SPD, monsterStats.getSpeed());
        assertEquals(EXPECTED_CR, monsterStats.getCriticalRate());
        assertEquals(EXPECTED_CD, monsterStats.getCriticalDamage());
        assertEquals(EXPECTED_RES, monsterStats.getResistance());
        assertEquals(EXPECTED_ACC, monsterStats.getAccuracy());
    }
}