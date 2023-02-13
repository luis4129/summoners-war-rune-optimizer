package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.exception.UnmappedRuneSetConversionException;
import com.optmizer.summonerwaroptimizer.model.RuneSet;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants.SwarfarmConstants.SWARFARM_RUNE_SET_CONVERSION_MAP;

@Service
public class RuneSetConversionService {

    public RuneSet toRuneSet(Integer swarfarmRuneSet) {
        var runeSet = SWARFARM_RUNE_SET_CONVERSION_MAP.get(swarfarmRuneSet);

        return Optional.ofNullable(runeSet)
            .orElseThrow(UnmappedRuneSetConversionException::new);
    }

}
