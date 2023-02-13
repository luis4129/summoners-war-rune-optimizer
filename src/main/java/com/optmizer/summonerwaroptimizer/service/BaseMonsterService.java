package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.exception.BaseMonsterNotFoundException;
import com.optmizer.summonerwaroptimizer.model.BaseMonster;
import com.optmizer.summonerwaroptimizer.repository.BaseMonsterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BaseMonsterService {

    @Autowired
    private BaseMonsterRepository baseMonsterRepository;

    public BaseMonster findBySwarfarmId(Long swarfarmId) {
        return baseMonsterRepository.findBySwarfarmId(swarfarmId)
            .orElseThrow(BaseMonsterNotFoundException::new);
    }

    public void saveAll(List<BaseMonster> baseMonsters) {
        baseMonsterRepository.saveAll(baseMonsters);
    }


}
