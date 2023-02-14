package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRuneFactory;
import com.optmizer.summonerwaroptimizer.model.rune.MainStatFactory;
import com.optmizer.summonerwaroptimizer.model.rune.PrefixStatFactory;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import com.optmizer.summonerwaroptimizer.model.rune.SubStatFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuneConversionServiceTest {

    @Mock
    private StatConversionService statConversionService;

    @Mock
    private RuneSetConversionService runeSetConversionService;

    @InjectMocks
    private RuneConversionService runeConversionService;

    @Test
    void shouldReturnRuneWhenConvertingSwarfarmRune() {
        //given
        var swarfarmRunes = SwarfarmRuneFactory.getValidSwarfarmRunes();
        var runeSet = RuneSet.VIOLENT;
        var mainStat = MainStatFactory.getValidMainStat();
        var prefixStat = PrefixStatFactory.getValidPrefixStat();
        var subStats = SubStatFactory.getValidSubStats();
        var firstSwarfarmRune = swarfarmRunes.get(0);

        when(statConversionService.toMainStat(anyList()))
            .thenReturn(mainStat);
        when(statConversionService.toPrefixStat(anyList()))
            .thenReturn(prefixStat);
        when(statConversionService.toSubStats(anyList()))
            .thenReturn(subStats);
        when(runeSetConversionService.toRuneSet(firstSwarfarmRune.getSet()))
            .thenReturn(runeSet);

        //when
        var rune = runeConversionService.toRunes(swarfarmRunes);

        var firstRune = rune.get(0);

        //then
        assertEquals(firstSwarfarmRune.getId(), firstRune.getId());
        assertEquals(runeSet, firstRune.getSet());
        assertEquals(firstSwarfarmRune.getSlot(), firstRune.getSlot());
        assertEquals(firstSwarfarmRune.getGrade(), firstRune.getGrade());
        assertEquals(firstSwarfarmRune.getLevel(), firstRune.getLevel());
        assertEquals(mainStat, firstRune.getMainStat());
        assertEquals(prefixStat, firstRune.getPrefixStat());
        assertEquals(subStats, firstRune.getSubStats());

    }
}