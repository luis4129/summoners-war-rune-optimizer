package com.optmizer.summonerwaroptimizer.repository;

import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuneEfficiencyRepository extends JpaRepository<RuneEfficiency, Long> {
    List<RuneEfficiency> findByBuildStrategy_Monster_SwarfarmId_OrderByEfficiencyDesc(Long swarfarmId);

}
