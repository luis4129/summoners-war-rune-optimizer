package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.optmizer.summonerwaroptimizer.exception.DatabaseIntegrationException;
import com.optmizer.summonerwaroptimizer.model.RuneFactory;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRuneFactory;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.RuneConversionService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuneImportServiceTest {

    @Mock
    private RuneConversionService runeConversionService;

    @Mock
    private RuneService runeService;

    @InjectMocks
    private RuneImportService runeImportService;

    @Test
    void shouldSaveConvertedRunes() {
        //given
        var swarfarmRunes = SwarfarmRuneFactory.getValidSwarfarmRunes();
        var runes = RuneFactory.getValidRunes();

        when(runeConversionService.toRunes(swarfarmRunes))
            .thenReturn(runes);

        //when
        runeImportService.importUnequippedRunes(swarfarmRunes);

        //then
        verify(runeService, times(1)).saveAll(runes);
    }

    @Test
    void shouldThrowIntegrationExceptionWhenRepositoryThrowsAnyException() {
        //given
        var swarfarmRunes = SwarfarmRuneFactory.getValidSwarfarmRunes();
        var runes = RuneFactory.getValidRunes();

        when(runeConversionService.toRunes(swarfarmRunes))
            .thenReturn(runes);
        doThrow(ConstraintViolationException.class)
            .when(runeService).saveAll(anyList());

        //when-then
        assertThrows(DatabaseIntegrationException.class, () -> runeImportService.importUnequippedRunes(swarfarmRunes));
    }
}