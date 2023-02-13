package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("runes")
public class RuneResource {

    @Autowired
    private RuneService runeService;

    @GetMapping
    public List<Rune> findAll() {
        return runeService.findAll();
    }

}
