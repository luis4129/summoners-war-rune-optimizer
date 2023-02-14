package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.repository.MonsterRepository;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.simulation.MonsterBuildService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MonsterService {

    @Autowired
    private MonsterRepository monsterRepository;

    @Autowired
    private MonsterBuildService monsterBuildService;

    public Monster getBySwarmFarmId(Long swarfarmId) {
        return monsterRepository.findBySwarfarmId(swarfarmId);
    }

    public List<Monster> get() {
        return monsterRepository.findAll();
    }

    public void saveAll(List<Monster> monsters) {
        monsterRepository.saveAll(monsters);
    }

    public MonsterStats getMonsterStats(Long swarfarmId) {
        var monster = getBySwarmFarmId(swarfarmId);

        return monsterBuildService.getMonsterStats(monster);

    }
}
