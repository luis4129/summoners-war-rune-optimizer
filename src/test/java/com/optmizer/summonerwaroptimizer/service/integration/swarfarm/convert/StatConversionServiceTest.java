package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRuneFactory;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatConversionServiceTest {

    @Mock
    private AttributeConversionService attributeConversionService;

    @InjectMocks
    private StatConversionService statConversionService;

    @Test
    void shouldReturnAttributeWhenConvertingPrimaryEffect() {
        //given
        var primaryEffect = SwarfarmRuneFactory.getValidSwarfarmAttribute();
        var primaryEffectValue = primaryEffect.get(1);

        when(attributeConversionService.toAttribute(anyInt()))
            .thenReturn(BonusAttribute.SPEED);

        //when
        var mainStat = statConversionService.toMainStat(primaryEffect);

        //then
        assertEquals(BonusAttribute.SPEED, mainStat.getBonusAttribute());
        assertEquals(primaryEffectValue, mainStat.getValue());
    }

    @Test
    void shouldReturnPrefixStatWhenConvertingPrefixEffect() {
        //given
        var prefixEffect = SwarfarmRuneFactory.getValidSwarfarmAttribute();
        var prefixEffectValue = prefixEffect.get(1);

        when(attributeConversionService.toAttribute(anyInt()))
            .thenReturn(BonusAttribute.SPEED);

        //when
        var prefixStat = statConversionService.toPrefixStat(prefixEffect);

        //then
        assertEquals(BonusAttribute.SPEED, prefixStat.getBonusAttribute());
        assertEquals(prefixEffectValue, prefixStat.getValue());
    }

    @Test
    void shouldReturnSubStatsWhenConvertingSecondaryEffects() {
        //given
        var secondaryEffects = SwarfarmRuneFactory.getValidSwarfarmSubStats();
        var secondaryEffectValue = secondaryEffects.get(0).get(1);
        var secondaryEffectIsEnchanted = secondaryEffects.get(0).get(2) == 1;
        var secondaryEffectGrindValue = secondaryEffects.get(0).get(3);

        when(attributeConversionService.toAttribute(anyInt()))
            .thenReturn(BonusAttribute.SPEED);

        //when
        var subStats = statConversionService.toSubStats(secondaryEffects);

        //then
        assertEquals(BonusAttribute.SPEED, subStats.get(0).getBonusAttribute());
        assertEquals(secondaryEffectValue, subStats.get(0).getValue());
        assertEquals(secondaryEffectGrindValue, subStats.get(0).getGrindValue());
        assertEquals(secondaryEffectIsEnchanted, subStats.get(0).isEnchanted());
        assertEquals(secondaryEffects.size(), subStats.size());
    }

    @Test
    void shouldReturnNullWhenConvertingEmptyPrefixEffect() {
        //given
        var prefixEffect = SwarfarmRuneFactory.getEmptySwarfarmAttribute();

        //when
        var prefixStat = statConversionService.toPrefixStat(prefixEffect);

        //then
        assertNull(prefixStat);
    }

    @Test
    void shouldReturnEmptyListWhenConvertingEmptySecondaryEffects() {
        //given
        var secondaryEffects = SwarfarmRuneFactory.getEmptySwarfarmSubStats();

        //when
        var subStats = statConversionService.toSubStats(secondaryEffects);

        //then
        assertTrue(subStats.isEmpty());

    }
}