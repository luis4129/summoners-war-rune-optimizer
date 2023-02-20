package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.resource.response.MonsterStats;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("monsters")
public class MonsterResource {

    @Autowired
    private MonsterService monsterService;

    @GetMapping
    public List<Monster> get() {
        return monsterService.findAll();
    }

    @GetMapping("/{id}")
    public Monster getById(@PathVariable("id") Long swarfarmId) {
        return monsterService.findBySwarmFarmId(swarfarmId);
    }

    @GetMapping("/{id}/stats")
    public MonsterStats getMonsterStats(@PathVariable("id") Long swarfarmId) {
        return monsterService.getMonsterStats(swarfarmId);
    }

    @GetMapping("{id}/efficiency")
    public List<RuneEfficiency> runeEfficiencies(@PathVariable("id") Long swarfarmId) {
        return monsterService.getMonsterEfficiency(swarfarmId);
    }

}
