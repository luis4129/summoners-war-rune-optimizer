package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import com.optmizer.summonerwaroptimizer.repository.RuneRepository;
import com.optmizer.summonerwaroptimizer.service.optimizer.efficiency.RuneEfficiencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RuneService {

    @Autowired
    private RuneRepository runeRepository;

    @Autowired
    private RuneEfficiencyService runeEfficiencyService;

    public List<Rune> findAll() {
        return runeRepository.findAll();
    }

    public void saveAll(List<Rune> runes) {
        runeRepository.saveAll(runes);
    }

    public Rune findBySwarfarmId(Long swarfarmId) {
        return runeRepository.findBySwarfarmId(swarfarmId);
    }

    public List<RuneEfficiency> findEfficienciesBySwarfarmId(Long swarfarmId) {
        return runeEfficiencyService.findByRuneSwarfarmId(swarfarmId);
    }

    public void unequipAllRunes() {
        runeRepository.findAll()
            .stream()
            .peek(rune -> rune.setBuild(null))
            .forEach(runeRepository::save);
    }
}
