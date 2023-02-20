package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreferenceType;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.repository.BuildStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BuildStrategyService {

    @Autowired
    private BuildStrategyRepository buildStrategyRepository;

    public List<BuildStrategy> findAll() {
        return buildStrategyRepository.findByOrderByPriority();
    }

    public void saveAll(List<BuildStrategy> buildStrategies) {
        buildStrategyRepository.saveAll(buildStrategies);
    }

    public List<MonsterAttribute> getLimitedAttributes(BuildStrategy buildStrategy) {
        return buildStrategy.getBuildPreferences()
            .stream()
            .filter(buildPreference -> buildPreference.getType().equals(BuildPreferenceType.WITHIN_REQUIRED_RANGE))
            .map(BuildPreference::getAttribute).toList();
    }

    private List<BonusAttribute> toBonusAttributes(MonsterAttribute monsterAttribute) {
        return Stream.of(BonusAttribute.values()).filter(bonusAttribute -> bonusAttribute.getMonsterAttribute().equals(monsterAttribute)).toList();
    }
}
