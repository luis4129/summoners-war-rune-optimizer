package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildStrategy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    private Monster monster;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<RuneSet> runeSets;

    @OneToMany(mappedBy = "buildStrategy", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<BuildPreference> buildPreferences;

    private Integer priority;

    @JsonGetter("monster")
    public String getMonsterName() {
        return this.monster.getBaseMonster().getName();
    }

    public List<MonsterAttribute> getUsefulAttributes() {
        return buildPreferences.stream()
            .map(BuildPreference::getAttribute)
            .toList();
    }

    public List<BonusAttribute> getUsefulAttributesBonus() {
        return buildPreferences.stream()
            .map(BuildPreference::getAttribute)
            .map(monsterAttribute -> Stream.of(BonusAttribute.values()).filter(bonusAttribute -> bonusAttribute.getMonsterAttribute().equals(monsterAttribute)).toList())
            .flatMap(Collection::stream)
            .toList();

    }

    public List<MonsterAttribute> getLimitedAttributes() {
        return buildPreferences.stream()
            .filter(buildPreference -> buildPreference.getType().equals(BuildPreferenceType.WITHIN_REQUIRED_RANGE))
            .map(BuildPreference::getAttribute).toList();
    }

}
