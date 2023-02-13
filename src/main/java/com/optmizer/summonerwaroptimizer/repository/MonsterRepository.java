package com.optmizer.summonerwaroptimizer.repository;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterRepository extends JpaRepository<Monster, Long> {
    Monster findBySwarfarmId(Long swarfarmId);
}
