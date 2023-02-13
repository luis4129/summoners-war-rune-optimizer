package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.optmizer.summonerwaroptimizer.exception.DatabaseIntegrationException;
import com.optmizer.summonerwaroptimizer.model.MonsterFactory;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmMonsterFactory;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.MonsterConversionService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonsterImportServiceTest {

    @Mock
    private MonsterConversionService monsterConversionService;

    @Mock
    private MonsterService monsterService;

    @InjectMocks
    private MonsterImportService monsterImportService;

    @Test
    void shouldSaveConvertedMonsters() {
        //given
        var swarfarmMonsters = SwarfarmMonsterFactory.getValidSwarfarmMonsters();
        var monsters = MonsterFactory.getValidMonsters();

        when(monsterConversionService.toMonsters(swarfarmMonsters))
            .thenReturn(monsters);

        //when
        monsterImportService.importMonstersAndTheirEquippedRunes(swarfarmMonsters);

        //then
        verify(monsterService, times(1)).saveAll(monsters);
    }

    @Test
    void shouldThrowIntegrationExceptionWhenRepositoryThrowsAnyException() {
        //given
        var swarfarmMonsters = SwarfarmMonsterFactory.getValidSwarfarmMonsters();
        var monsters = MonsterFactory.getValidMonsters();

        when(monsterConversionService.toMonsters(anyList()))
            .thenReturn(monsters);
        doThrow(ConstraintViolationException.class)
            .when(monsterService).saveAll(anyList());

        //when-then
        assertThrows(DatabaseIntegrationException.class, () -> monsterImportService.importMonstersAndTheirEquippedRunes(swarfarmMonsters));
    }
}