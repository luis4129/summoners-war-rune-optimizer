package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.Monster;
import com.optmizer.summonerwaroptimizer.service.MonsterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("monsters")
public class MonsterResource {

    @Autowired
    private MonsterService monsterService;

    @GetMapping
    public List<Monster> findAll() {
        return monsterService.findAll();
    }

}
