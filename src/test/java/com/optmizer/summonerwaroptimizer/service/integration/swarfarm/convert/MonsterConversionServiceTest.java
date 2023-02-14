package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.build.BuildFactory;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmMonsterFactory;
import com.optmizer.summonerwaroptimizer.model.monster.BaseMonsterFactory;
import com.optmizer.summonerwaroptimizer.service.BaseMonsterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonsterConversionServiceTest {

    @Mock
    private BuildConversionService buildConversionService;

    @Mock
    private BaseMonsterService baseMonsterService;

    @InjectMocks
    private MonsterConversionService monsterConversionService;

    @Test
    void shouldReturnRuneWhenConvertingSwarfarmRune() {
        //given
        var swarfarmMonsters = SwarfarmMonsterFactory.getValidSwarfarmMonsters();
        var build = BuildFactory.getValidBuild();
        var baseMonster = BaseMonsterFactory.getValidBaseMonster();
        var firstSwarfarmMonster = swarfarmMonsters.get(0);

        when(buildConversionService.toBuild(anyList()))
            .thenReturn(build);
        when(baseMonsterService.findBySwarfarmId(firstSwarfarmMonster.getMasterId()))
            .thenReturn(baseMonster);

        //when
        var monster = monsterConversionService.toMonsters(swarfarmMonsters);
        var firstMonster = monster.get(0);

        //then
        assertEquals(firstSwarfarmMonster.getId(), firstMonster.getSwarfarmId());
        assertEquals(firstSwarfarmMonster.getGrade(), firstMonster.getGrade());
        assertEquals(firstSwarfarmMonster.getLevel(), firstMonster.getLevel());
        assertEquals(build, firstMonster.getBuild());
        assertEquals(baseMonster, firstMonster.getBaseMonster());

    }
}