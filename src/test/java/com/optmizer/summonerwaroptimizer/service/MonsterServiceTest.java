package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterFactory;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterStatsFactory;
import com.optmizer.summonerwaroptimizer.repository.MonsterRepository;
import com.optmizer.summonerwaroptimizer.service.simulation.MonsterBuildService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonsterServiceTest {

    @Mock
    private MonsterRepository monsterRepository;

    @Mock
    private MonsterBuildService monsterBuildService;

    @InjectMocks
    private MonsterService monsterService;

    private static final Long SWARFARM_ID = 123L;

    @Test
    void shouldCallDelegateFindAllToRepository() {
        //given
        var expectedMonsters = MonsterFactory.getValidMonsters();

        when(monsterRepository.findAll())
            .thenReturn(expectedMonsters);

        //when
        var actualMonsters = monsterService.findAll();

        //then
        assertEquals(expectedMonsters, actualMonsters);
    }

    @Test
    void shouldCallDelegateFindByIdToRepository() {
        //given
        var expectedMonster = MonsterFactory.getValidMonster();

        when(monsterRepository.findBySwarfarmId(anyLong()))
            .thenReturn(expectedMonster);

        //when
        var actualMonsters = monsterService.findBySwarmFarmId(SWARFARM_ID);

        //then
        assertEquals(expectedMonster, actualMonsters);
    }

    @Test
    void shouldCallDelegateGetMonsterStatsToSpecificService() {
        //given
        var monster = MonsterFactory.getValidMonster();
        var expectedMonsterStats = MonsterStatsFactory.getValidMonsterStats();

        when(monsterRepository.findBySwarfarmId(anyLong()))
            .thenReturn(monster);
        when(monsterBuildService.getMonsterStats(any(Monster.class)))
            .thenReturn(expectedMonsterStats);

        //when
        var actualMonsterStats = monsterService.getMonsterStats(SWARFARM_ID);

        //then
        assertEquals(expectedMonsterStats, actualMonsterStats);
    }

    @Test
    void shouldCallDelegateSaveAllToRepository() {
        //when
        monsterService.saveAll(anyList());

        //then
        verify(monsterRepository, times(1)).saveAll(anyList());
    }
}