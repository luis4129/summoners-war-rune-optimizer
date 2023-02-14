package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optmizer.summonerwaroptimizer.exception.BeastiaryImportIntegrationException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.FileFactory;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmAccount;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmBaseMonster;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmBaseMonsterFactory;
import com.optmizer.summonerwaroptimizer.model.monster.BaseMonsterFactory;
import com.optmizer.summonerwaroptimizer.service.BaseMonsterService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.BaseMonsterConversionService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseMonsterImportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FileImportService fileImportService;

    @Mock
    private BaseMonsterConversionService baseMonsterConversionService;

    @Mock
    private BaseMonsterService baseMonsterService;

    @InjectMocks
    private BaseMonsterImportService baseMonsterImportService;

    @Test
    void shouldImportBaseMonstersWhenFileDataIsValid() throws IOException {
        //given
        var file = FileFactory.getValidFile();

        when(fileImportService.getFile(anyString()))
            .thenReturn(file);
        when(objectMapper.readValue(file, SwarfarmBaseMonster[].class))
            .thenReturn(SwarfarmBaseMonsterFactory.getValidBaseMonstersArray());
        when(baseMonsterConversionService.toBaseMonsters(anyList()))
            .thenReturn(BaseMonsterFactory.getValidBaseMonsters());

        //when
        baseMonsterImportService.importBestiaryData();

        //then
        verify(baseMonsterService, times(1)).saveAll(anyList());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileImportingFiles() {
        //given
        when(fileImportService.getFile(anyString()))
            .thenThrow(NullPointerException.class);

        //when
        assertThrows(BeastiaryImportIntegrationException.class, () -> baseMonsterImportService.importBestiaryData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileConvertingFileToDTO() throws IOException {
        //given
        var file = FileFactory.getValidFile();

        when(fileImportService.getFile(anyString()))
            .thenReturn(file);
        when(objectMapper.readValue(file, SwarfarmAccount.class))
            .thenThrow(JsonMappingException.class);

        //when
        assertThrows(BeastiaryImportIntegrationException.class, () -> baseMonsterImportService.importBestiaryData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileImportingBaseMonsters() throws IOException {
        //given
        var file = FileFactory.getValidFile();

        when(fileImportService.getFile(anyString()))
            .thenReturn(file);
        when(objectMapper.readValue(file, SwarfarmBaseMonster[].class))
            .thenReturn(SwarfarmBaseMonsterFactory.getValidBaseMonstersArray());
        when(baseMonsterConversionService.toBaseMonsters(anyList()))
            .thenThrow(ConstraintViolationException.class);

        //when
        assertThrows(BeastiaryImportIntegrationException.class, () -> baseMonsterImportService.importBestiaryData());
    }
}