package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.MonsterFactory;
import com.optmizer.summonerwaroptimizer.repository.MonsterRepository;
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

    @InjectMocks
    private MonsterService monsterService;

    @Test
    void shouldCallRepositoryToFindAll() {
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
    void shouldCallRepositoryToSaveAll() {
        //when
        monsterService.saveAll(anyList());

        //then
        verify(monsterRepository, times(1)).saveAll(anyList());
    }
}