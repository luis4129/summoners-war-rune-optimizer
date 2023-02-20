package com.optmizer.summonerwaroptimizer.resource;

import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.service.RuneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{swarfarmId}")
    public Rune findBySwarfarmId(@PathVariable("swarfarmId") Long swarfarmId) {
        return runeService.findBySwarfarmId(swarfarmId);
    }

    @GetMapping("/{swarfarmId}/efficiencies")
    public List<RuneEfficiency> findEfficienciesBySwarfarmId(@PathVariable("swarfarmId") Long swarfarmId) {
        return runeService.findEfficienciesBySwarfarmId(swarfarmId);
    }

}
