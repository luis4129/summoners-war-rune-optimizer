package com.optmizer.summonerwaroptimizer.service.optimizer.efficiency;

import com.optmizer.summonerwaroptimizer.model.optimizer.efficiency.RuneEfficiency;
import com.optmizer.summonerwaroptimizer.repository.RuneEfficiencyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RuneEfficiencyService {

    @Autowired
    private RuneEfficiencyRepository runeEfficiencyRepository;

    public void save(RuneEfficiency runeEfficiency) {
        runeEfficiencyRepository.save(runeEfficiency);
    }

    public List<RuneEfficiency> findByMonsterSwarfarmId(Long swarfarmId) {
        return runeEfficiencyRepository.findByBuildStrategy_Monster_SwarfarmId_OrderByCompleteEfficiencyDesc(swarfarmId);
    }

    public List<RuneEfficiency> findByRuneSwarfarmId(Long swarfarmId) {
        return Collections.emptyList();
        //return runeEfficiencyRepository.findByRuneSwarfarmIdOrderByEfficiencyDesc(swarfarmId);
        //List<RuneEfficiency> findByRuneSwarfarmIdOrderByEfficiencyDesc(Long swarfarmId);
    }


}
