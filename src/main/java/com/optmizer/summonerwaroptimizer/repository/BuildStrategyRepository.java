package com.optmizer.summonerwaroptimizer.repository;

import com.optmizer.summonerwaroptimizer.model.optimizer.BuildStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildStrategyRepository extends JpaRepository<BuildStrategy, Long> {

    List<BuildStrategy> findByOrderByPriority();

    BuildStrategy findByMonster_SwarfarmId(Long swarfarmId);
}
