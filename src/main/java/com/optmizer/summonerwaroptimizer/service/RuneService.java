package com.optmizer.summonerwaroptimizer.service;

import com.optmizer.summonerwaroptimizer.model.Rune;
import com.optmizer.summonerwaroptimizer.repository.RuneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RuneService {

    @Autowired
    private RuneRepository runeRepository;

    public List<Rune> findAll() {
        return runeRepository.findAll();
    }

    public void saveAll(List<Rune> runes) {
        runeRepository.saveAll(runes);
    }
}
