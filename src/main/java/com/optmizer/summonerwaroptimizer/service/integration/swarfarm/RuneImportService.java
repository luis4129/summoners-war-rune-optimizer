package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.optmizer.summonerwaroptimizer.exception.DatabaseIntegrationException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert.RuneConversionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class RuneImportService {

    @Autowired
    private RuneConversionService runeConversionService;

    @Autowired
    private RuneService runeService;

    public void importUnequippedRunes(List<SwarfarmRune> swarfarmRunes) {
        try {
            var runes = runeConversionService.toRunes(swarfarmRunes);

            runeService.saveAll(runes);
        } catch (Exception ex) {
            log.error("c=RuneImportService m=importUnequippedRunes message=Error while importing unequipped runes on database");
            throw new DatabaseIntegrationException(ex);
        }
    }


}
