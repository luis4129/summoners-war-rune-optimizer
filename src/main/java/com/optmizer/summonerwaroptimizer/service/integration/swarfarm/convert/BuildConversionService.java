package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRune;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildConversionService {

    @Autowired
    private RuneConversionService runeConversionService;

    public Build toBuild(List<SwarfarmRune> swarfarmRunes) {
        var equippedRunes = runeConversionService.toRunes(swarfarmRunes);

        var build = Build.builder()
            .runes(equippedRunes)
            .build();

        equippedRunes.forEach(rune -> rune.setBuild(build));

        return build;

    }
}
