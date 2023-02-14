package com.optmizer.summonerwaroptimizer.service.integration.swarfarm.convert;

import com.optmizer.summonerwaroptimizer.exception.UnmappedAttributeConversionException;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants.SwarfarmConstants.SWARFARM_ATTRIBUTE_CONVERSION_MAP;

@Service
public class AttributeConversionService {

    public BonusAttribute toAttribute(Integer swarfarmAttribute) {
        var attribute = SWARFARM_ATTRIBUTE_CONVERSION_MAP.get(swarfarmAttribute);

        return Optional.ofNullable(attribute)
            .orElseThrow(UnmappedAttributeConversionException::new);
    }

}
