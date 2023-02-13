package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optmizer.summonerwaroptimizer.exception.BeastiaryImportIntegrationException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmBaseMonster;
import com.optmizer.summonerwaroptimizer.service.BaseMonsterService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.BaseMonsterConversionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants.SwarfarmConstants.BESTIARY_DATA_FILE;

@Slf4j
@Service
public class BaseMonsterImportService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileImportService fileImportService;

    @Autowired
    private BaseMonsterConversionService baseMonsterConversionService;

    @Autowired
    private BaseMonsterService baseMonsterService;

    @Transactional
    public void importBestiaryData() {
        try {
            var swarfarmBaseMonsters = getBaseMonstersData();
            var baseMonsters = baseMonsterConversionService.toBaseMonsters(swarfarmBaseMonsters);

            baseMonsterService.saveAll(baseMonsters);
            log.info("c=BaseMonsterConversionService m=importBestiaryData message=Beastiary data have been successfully imported");
        } catch (Exception ex) {
            log.error("c=BaseMonsterConversionService m=importBestiaryData message=Failed to import beastiary data due to some integration error");
            throw new BeastiaryImportIntegrationException(ex);
        }
    }

    private List<SwarfarmBaseMonster> getBaseMonstersData() throws IOException {
        var beastiaryFile = fileImportService.getFile(BESTIARY_DATA_FILE);

        return List.of(objectMapper.readValue(beastiaryFile, SwarfarmBaseMonster[].class));
    }
}
