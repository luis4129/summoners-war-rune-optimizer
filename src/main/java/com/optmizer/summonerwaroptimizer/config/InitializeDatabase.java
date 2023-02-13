package com.optmizer.summonerwaroptimizer.config;

import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.AccountImportService;
import com.optmizer.summonerwaroptimizer.service.integration.swarfarm.BaseMonsterImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InitializeDatabase implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private BaseMonsterImportService baseMonsterImportService;

    @Autowired
    private AccountImportService accountImportService;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        baseMonsterImportService.importBestiaryData();
        accountImportService.importAccountData();
    }
}
