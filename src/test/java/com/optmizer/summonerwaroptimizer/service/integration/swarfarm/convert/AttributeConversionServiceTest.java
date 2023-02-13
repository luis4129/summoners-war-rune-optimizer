package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.exception.UnmappedAttributeConversionException;
import com.optmizer.summonerwaroptimizer.model.Attribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AttributeConversionServiceTest {

    @InjectMocks
    private AttributeConversionService attributeConversionService;

    private static final Integer SWARFARM_FLAT_HIT_POINTS_INDEX = 1;
    private static final Integer SWARFARM_HIT_POINTS_INDEX = 2;
    private static final Integer SWARFARM_FLAT_ATTACK_INDEX = 3;
    private static final Integer SWARFARM_ATTACK_INDEX = 4;
    private static final Integer SWARFARM_FLAT_DEFENSE_INDEX = 5;
    private static final Integer SWARFARM_DEFENSE_INDEX = 6;
    private static final Integer SWARFARM_SPEED_INDEX = 8;
    private static final Integer SWARFARM_CRITICAL_RATE_INDEX = 9;
    private static final Integer SWARFARM_CRITICAL_DAMAGE_POINTS_INDEX = 10;
    private static final Integer SWARFARM_RESISTANCE_INDEX = 11;
    private static final Integer SWARFARM_ACCURACY_INDEX = 12;
    private static final Integer SWARFARM_UNMAPPED_INDEX = 7;

    @Test
    void shouldReturnFlatHitPointsAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_FLAT_HIT_POINTS_INDEX);

        //then
        assertEquals(Attribute.FLAT_HIT_POINTS, attribute);
    }

    @Test
    void shouldReturnHitPointsAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_HIT_POINTS_INDEX);

        //then
        assertEquals(Attribute.HIT_POINTS, attribute);
    }

    @Test
    void shouldReturnFlatAttackAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_FLAT_ATTACK_INDEX);

        //then
        assertEquals(Attribute.FLAT_ATTACK, attribute);
    }

    @Test
    void shouldReturnAttackAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_ATTACK_INDEX);

        //then
        assertEquals(Attribute.ATTACK, attribute);
    }

    @Test
    void shouldReturnFlatDefenseAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_FLAT_DEFENSE_INDEX);

        //then
        assertEquals(Attribute.FLAT_DEFENSE, attribute);
    }

    @Test
    void shouldReturnDefenseAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_DEFENSE_INDEX);

        //then
        assertEquals(Attribute.DEFENSE, attribute);
    }

    @Test
    void shouldReturnSpeedAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_SPEED_INDEX);

        //then
        assertEquals(Attribute.SPEED, attribute);
    }

    @Test
    void shouldReturnCriticalRateAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_CRITICAL_RATE_INDEX);

        //then
        assertEquals(Attribute.CRITICAl_RATE, attribute);
    }

    @Test
    void shouldReturnCriticalDamageAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_CRITICAL_DAMAGE_POINTS_INDEX);

        //then
        assertEquals(Attribute.CRITICAL_DAMAGE, attribute);
    }

    @Test
    void shouldReturnResistanceAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_RESISTANCE_INDEX);

        //then
        assertEquals(Attribute.RESISTANCE, attribute);
    }

    @Test
    void shouldReturnAccuracyAttribute() {
        //when
        var attribute = attributeConversionService.toAttribute(SWARFARM_ACCURACY_INDEX);

        //then
        assertEquals(Attribute.ACCURACY, attribute);
    }

    @Test
    void shouldThrowExceptionWhenIndexIsUnmapped() {
        //when
        assertThrows(UnmappedAttributeConversionException.class, () -> attributeConversionService.toAttribute(SWARFARM_UNMAPPED_INDEX));
    }
}