package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.RuneFactory;
import com.optmizer.summonerwaroptimizer.repository.RuneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuneServiceTest {

    @Mock
    private RuneRepository runeRepository;

    @InjectMocks
    private RuneService runeService;

    @Test
    void shouldCallRepositoryToFindAll() {
        //given
        var expectedRunes = RuneFactory.getValidRunes();

        when(runeRepository.findAll())
            .thenReturn(expectedRunes);

        //when
        var actualRunes = runeService.findAll();

        //then
        assertEquals(expectedRunes, actualRunes);
    }

    @Test
    void shouldCallRepositoryToSaveAll() {
        //when
        runeService.saveAll(anyList());

        //then
        verify(runeRepository, times(1)).saveAll(anyList());
    }
}