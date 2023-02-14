package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.model.rune.MainStat;
import com.optmizer.summonerwaroptimizer.model.rune.PrefixStat;
import com.optmizer.summonerwaroptimizer.model.rune.SubStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants.SwarfarmConstants.*;

@Service
public class StatConversionService {

    @Autowired
    private AttributeConversionService attributeConversionService;

    public MainStat toMainStat(List<Integer> primaryEffect) {
        return MainStat.builder()
            .bonusAttribute(attributeConversionService.toAttribute(primaryEffect.get(ATTRIBUTE_INDEX)))
            .value(primaryEffect.get(VALUE_INDEX))
            .build();
    }

    public PrefixStat toPrefixStat(List<Integer> prefixEffect) {
        if (isEmptyStat(prefixEffect))
            return null;

        return PrefixStat.builder()
            .bonusAttribute(attributeConversionService.toAttribute(prefixEffect.get(ATTRIBUTE_INDEX)))
            .value(prefixEffect.get(VALUE_INDEX))
            .build();
    }

    public List<SubStat> toSubStats(List<List<Integer>> secondaryEffects) {
        return secondaryEffects
            .stream()
            .map(this::toSubStat)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private SubStat toSubStat(List<Integer> secondaryEffect) {
        if (isEmptyStat(secondaryEffect))
            return null;

        return SubStat.builder()
            .bonusAttribute(attributeConversionService.toAttribute(secondaryEffect.get(ATTRIBUTE_INDEX)))
            .value(secondaryEffect.get(VALUE_INDEX))
            .enchanted(secondaryEffect.get(IS_ENCHANTED_INDEX) == 1)
            .grindValue(secondaryEffect.get(GRIND_VALUE_INDEX))
            .build();
    }

    private boolean isEmptyStat(List<Integer> swarfarmEffect) {
        return EMPTY_STAT.equals(swarfarmEffect.get(ATTRIBUTE_INDEX));
    }

}
