package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.repository.MonsterRepository;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.optimizer.efficiency.RuneEfficiencyService;
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

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    public Monster findBySwarmFarmId(Long swarfarmId) {
        return monsterRepository.findBySwarfarmId(swarfarmId);
    }

    public List<Monster> findAll() {
        return monsterRepository.findAll();
    }

    public void save(Monster monster) {
        monsterRepository.save(monster);
    }

    public void saveAll(List<Monster> monsters) {
        monsterRepository.saveAll(monsters);
    }

    public MonsterStats getMonsterStats(Long swarfarmId) {
        var monster = findBySwarmFarmId(swarfarmId);

        return monsterBuildService.getMonsterStats(monster);
    }

    public List<RuneEfficiency> getMonsterEfficiency(Long swarfarmId) {
        return runeEfficiencyService.findByMonsterSwarfarmId(swarfarmId);
    }
}
