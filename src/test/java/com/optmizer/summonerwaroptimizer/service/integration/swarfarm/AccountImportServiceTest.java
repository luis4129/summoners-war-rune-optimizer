package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.optmizer.summonerwaroptimizer.exception.AccountDataNotFoundException;
import com.optmizer.summonerwaroptimizer.exception.AccountImportIntegrationException;
import com.optmizer.summonerwaroptimizer.exception.UnmappedAttributeConversionException;
import com.optmizer.summonerwaroptimizer.exception.UnmappedRuneSetConversionException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.FileFactory;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmAccount;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmAccountFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountImportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FileImportService fileImportService;

    @Mock
    private MonsterImportService monsterImportService;

    @Mock
    private RuneImportService runeImportService;

    @InjectMocks
    private AccountImportService accountImportService;

    @Test
    void shouldImportMonstersAndRunesWhenFileDataAreAllValid() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());

        //when
        accountImportService.importAccountData();

        //then
        verify(monsterImportService, times(1)).importMonstersAndTheirEquippedRunes(anyList());
        verify(runeImportService, times(1)).importUnequippedRunes(anyList());
    }

    @Test
    void shouldThrowAccountDataNotFoundExceptionWhenThereIsNoFile() {
        //given
        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(Collections.emptyList());

        //when
        assertThrows(AccountDataNotFoundException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowUnmappedAttributeConversionExceptionWhenMonstersRunesHaveInvalidAttribute() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doThrow(UnmappedAttributeConversionException.class)
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());

        //when
        assertThrows(UnmappedAttributeConversionException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowUnmappedAttributeConversionExceptionWhenUnequippedRunesHaveInvalidAttribute() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doNothing()
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());
        doThrow(UnmappedAttributeConversionException.class)
            .when(runeImportService).importUnequippedRunes(anyList());

        //when
        assertThrows(UnmappedAttributeConversionException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowUnmappedRuneSetConversionExceptionWhenMonstersRunesHaveInvalidRuneSet() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doThrow(UnmappedRuneSetConversionException.class)
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());

        //when
        assertThrows(UnmappedRuneSetConversionException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowUnmappedRuneSetConversionExceptionWhenUnequippedRunesHaveInvalidRuneSet() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doNothing()
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());
        doThrow(UnmappedRuneSetConversionException.class)
            .when(runeImportService).importUnequippedRunes(anyList());

        //when
        assertThrows(UnmappedRuneSetConversionException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileImportingFiles() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenThrow(JsonMappingException.class);

        //when
        assertThrows(AccountImportIntegrationException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileConvertingFileToDTO() throws IOException {
        //given
        when(fileImportService.getFilesInFolder(anyString()))
            .thenThrow(NullPointerException.class);

        //when
        assertThrows(AccountImportIntegrationException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileImportingMonsters() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doThrow(ConstraintViolationException.class)
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());

        //when
        assertThrows(AccountImportIntegrationException.class, () -> accountImportService.importAccountData());
    }

    @Test
    void shouldThrowAccountImportIntegrationExceptionWhenUnpredictedExceptionHappensWhileImportingRunes() throws IOException {
        //given
        var files = FileFactory.getValidFiles();
        var firstFile = files.get(0);

        when(fileImportService.getFilesInFolder(anyString()))
            .thenReturn(files);
        when(objectMapper.readValue(firstFile, SwarfarmAccount.class))
            .thenReturn(SwarfarmAccountFactory.getValidSwarfarmAccount());
        doNothing()
            .when(monsterImportService).importMonstersAndTheirEquippedRunes(anyList());
        doThrow(ConstraintViolationException.class)
            .when(runeImportService).importUnequippedRunes(anyList());

        //when
        assertThrows(AccountImportIntegrationException.class, () -> accountImportService.importAccountData());
    }
}