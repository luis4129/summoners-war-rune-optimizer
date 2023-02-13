package com.optmizer.summonerwaroptimizer.model.integration.swarfarm;

public class SwarfarmAccountFactory {

    public static SwarfarmAccount getValidSwarfarmAccount() {
        return SwarfarmAccount.builder()
            .monsters(SwarfarmMonsterFactory.getValidSwarfarmMonsters())
            .unequippedRunes(SwarfarmRuneFactory.getValidSwarfarmRunes())
            .build();
    }

}
