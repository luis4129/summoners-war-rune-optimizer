package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.optmizer.summonerwaroptimizer.exception.DatabaseIntegrationException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmMonster;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.MonsterConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class MonsterImportService {

    @Autowired
    private MonsterConversionService monsterConversionService;

    @Autowired
    private MonsterService monsterService;

    public void importMonstersAndTheirEquippedRunes(List<SwarfarmMonster> swarfarmMonsters) {
        try {
            var monsters = monsterConversionService.toMonsters(swarfarmMonsters);

            monsterService.saveAll(monsters);
        } catch (Exception ex) {
            log.error("c=MonsterImportService m=importMonsters message=Error while importing monsters and their runes on database");
            throw new DatabaseIntegrationException(ex);
        }

    }


}
