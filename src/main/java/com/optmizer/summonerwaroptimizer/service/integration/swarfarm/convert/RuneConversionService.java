package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmRune;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuneConversionService {

    @Autowired
    private StatConversionService statConversionService;

    @Autowired
    private RuneSetConversionService runeSetConversionService;

    public List<Rune> toRunes(List<SwarfarmRune> swarfarmRunes) {
        return swarfarmRunes.stream()
            .map(this::toRune)
            .collect(Collectors.toList());
    }

    private Rune toRune(SwarfarmRune swarfarmRune) {
        var mainStat = statConversionService.toMainStat(swarfarmRune.getPrimaryEffect());
        var prefixStat = statConversionService.toPrefixStat(swarfarmRune.getPrefixEffect());
        var subStats = statConversionService.toSubStats(swarfarmRune.getSecondaryEffects());
        var runeSet = runeSetConversionService.toRuneSet(swarfarmRune.getSet());

        var swarfarmGrade = swarfarmRune.getGrade();
        var isAncient = isAncient(swarfarmGrade);
        var grade = toGrade(swarfarmGrade, isAncient);


        var rune = Rune.builder()
            .swarfarmId(swarfarmRune.getId())
            .set(runeSet)
            .slot(swarfarmRune.getSlot())
            .grade(grade)
            .level(swarfarmRune.getLevel())
            .mainStat(mainStat)
            .prefixStat(prefixStat)
            .subStats(subStats)
            .build();

        subStats.forEach(subStat -> subStat.setRune(rune));

        return rune;
    }

    private boolean isAncient(Integer grade) {
        return grade > 10;
    }

    private Integer toGrade(Integer swarfarmGrade, boolean isAncient) {
        return isAncient ? swarfarmGrade - 10 : swarfarmGrade;
    }
}
