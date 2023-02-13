package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.repository.MonsterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MonsterService {

    @Autowired
    private MonsterRepository monsterRepository;

    public Monster findBySwarfarmId(Long swarfarmId) {
        return monsterRepository.findBySwarfarmId(swarfarmId);
    }

    public List<Monster> findAll() {
        return monsterRepository.findAll();
    }

    public void saveAll(List<Monster> monsters) {
        monsterRepository.saveAll(monsters);
    }
}
