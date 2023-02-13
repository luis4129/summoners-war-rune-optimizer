package com.optmizer.summonerwaroptimizer.service.integration.swarfarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.optmizer.summonerwaroptimizer.exception.AccountDataNotFoundException;
import com.optmizer.summonerwaroptimizer.exception.AccountImportIntegrationException;
import com.optmizer.summonerwaroptimizer.exception.UnmappedAttributeConversionException;
import com.optmizer.summonerwaroptimizer.exception.UnmappedRuneSetConversionException;
import com.optmizer.summonerwaroptimizer.model.integration.swarfarm.SwarfarmAccount;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.optmizer.summonerwaroptimizer.service.integration.swarfarm.constants.SwarfarmConstants.ACCOUNT_DATA_FOLDER;

@Slf4j
@Service
public class AccountImportService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileImportService fileImportService;

    @Autowired
    private MonsterImportService monsterImportService;

    @Autowired
    private RuneImportService runeImportService;

    @Transactional
    public void importAccountData() {
        try {
            var swarfarmAccount = getSwarfarmAccountFromFile();

            monsterImportService.importMonstersAndTheirEquippedRunes(swarfarmAccount.getMonsters());
            runeImportService.importUnequippedRunes(swarfarmAccount.getUnequippedRunes());
            log.info("c=AccountImportService m=importAccountData message=Swarfarm data have been successfully imported");
        } catch (AccountDataNotFoundException ex) {
            log.error("c=AccountImportService m=importAccountData error=AccountDataNotFoundException message=No account data was found");
            throw ex;
        } catch (UnmappedAttributeConversionException ex) {
            log.error("c=AccountImportService m=importAccountData error=UnmappedAttributeConversionException message=Unknown code was ");
            throw ex;
        } catch (UnmappedRuneSetConversionException ex) {
            log.error("c=AccountImportService m=importAccountData error=UnmappedRuneSetConversionException message=Unknown code was ");
            throw ex;
        } catch (Exception ex) {
            log.error("c=AccountImportService m=importAccountData message=Failed to import account data due to some integration error");
            throw new AccountImportIntegrationException(ex);
        }
    }

    private SwarfarmAccount getSwarfarmAccountFromFile() throws IOException {
        var accountDataFile = fileImportService.getFilesInFolder(ACCOUNT_DATA_FOLDER)
            .stream()
            .findFirst()
            .orElseThrow(AccountDataNotFoundException::new);

        return objectMapper.readValue(accountDataFile, SwarfarmAccount.class);
    }
}
