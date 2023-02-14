package com.optmizer.summonerwaroptimizer.service.simulation.bonus;

import com.optmizer.summonerwaroptimizer.model.build.BuildFactory;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SubStatBonusServiceTest {

    @InjectMocks
    private SubStatBonusService subStatBonusService;

    private static final MonsterAttribute SPEED_ATTRIBUTE = MonsterAttribute.SPEED;
    private static final MonsterAttribute ATTACK_ATTRIBUTE = MonsterAttribute.ATTACK;
    private static final Integer BASE_SPEED_VALUE = 115;
    private static final Integer BASE_ATTACK_VALUE = 600;
    private static final BigDecimal SPEED_BONUS = BigDecimal.valueOf(11);
    private static final BigDecimal ATTACK_BONUS = BigDecimal.valueOf(108).setScale(2, RoundingMode.UP);

    @Test
    void shouldCalculateSubStatsBonusWithSumAggregator() {
        //given
        var build = BuildFactory.getBuildWithSpeedAttribute();

        //when
        var subStatsBonus = subStatBonusService.getSubStatsBonus(SPEED_ATTRIBUTE, BASE_SPEED_VALUE, build);

        //then
        assertEquals(SPEED_BONUS, subStatsBonus);
    }

    @Test
    void shouldCalculateSubStatBonusWithMultiplyAggregator() {
        //given
        var build = BuildFactory.getBuildWithAttackAttribute();

        //when
        var subStatsBonus = subStatBonusService.getSubStatsBonus(ATTACK_ATTRIBUTE, BASE_ATTACK_VALUE, build);

        //then
        assertEquals(ATTACK_BONUS, subStatsBonus);
    }
}