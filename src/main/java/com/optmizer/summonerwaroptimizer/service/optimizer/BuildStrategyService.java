package com.optmizer.summonerwaroptimizer.service.optimizer;

import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreference;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildPreferenceType;
import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.repository.BuildStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
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

    @Cacheable("strategy-monster-attributes")
    public Map<Integer, List<MonsterAttribute>> getUsefulAttributesByPriorityMap(BuildStrategy buildStrategy) {
        int lowestPriority = buildStrategy.getBuildPreferences().stream().map(BuildPreference::getPriority).max(Comparator.naturalOrder()).orElse(0);
        var usefulAttributesByPriorityMap = new HashMap<Integer, List<MonsterAttribute>>();

        for (int priority = 0; priority <= lowestPriority; priority++) {
            int finalPriority = priority;
            var usefulAttributes = buildStrategy.getBuildPreferences()
                .stream()
                .filter(buildPreference -> buildPreference.getPriority() > finalPriority || buildPreference.getPriority() == 0)
                .map(BuildPreference::getAttribute)
                .toList();

            usefulAttributesByPriorityMap.put(priority, usefulAttributes);
        }

        return usefulAttributesByPriorityMap;
    }

    @Cacheable("strategy-bonus-attributes")
    public Map<Integer, List<BonusAttribute>> getUsefulAttributesBonusByPriorityMap(BuildStrategy buildStrategy) {
        return getUsefulAttributesByPriorityMap(buildStrategy)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new ArrayList<>(entry.getValue().stream().map(this::toBonusAttributes).flatMap(Collection::stream).toList())
            ));
    }

    @Cacheable("strategy-limited-attributes")
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
