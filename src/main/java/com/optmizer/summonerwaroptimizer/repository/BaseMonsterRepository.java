package com.optmizer.summonerwaroptimizer.repository;

import com.optmizer.summonerwaroptimizer.model.monster.BaseMonster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseMonsterRepository extends JpaRepository<BaseMonster, Long> {

    Optional<BaseMonster> findBySwarfarmId(Long swarfarmId);
}
