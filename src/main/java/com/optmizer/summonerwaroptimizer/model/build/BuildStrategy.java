package com.optmizer.summonerwaroptimizer.model.build;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @ElementCollection
    private List<RuneSet> runeSets;

    @OneToMany(mappedBy = "buildStrategy", cascade = CascadeType.ALL)
    private List<BuildPreference> buildPreferences;

    @JsonGetter("monster")
    public String getMonsterName() {
        return this.monster.getBaseMonster().getName();
    }

}
