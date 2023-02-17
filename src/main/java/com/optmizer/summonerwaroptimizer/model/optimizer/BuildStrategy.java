package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.BonusAttribute;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
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

    @JsonGetter("monster")
    public String getMonsterName() {
        return this.monster.getBaseMonster().getName();
    }

    @JsonIgnore
    public List<MonsterAttribute> getUsefulAttributes() {
        return buildPreferences.stream()
            .map(BuildPreference::getAttribute)
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<BonusAttribute> getUsefulAttributesBonus() {
        var monsterAttributes = buildPreferences.stream().map(BuildPreference::getAttribute).toList();

        return Arrays.stream(BonusAttribute.values())
            .filter(bonusAttribute -> monsterAttributes.contains(bonusAttribute.getMonsterAttribute()))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean hasRequiredRuneSets() {
        return Optional.of(runeSets).map(List::isEmpty).orElse(true);
    }
}
