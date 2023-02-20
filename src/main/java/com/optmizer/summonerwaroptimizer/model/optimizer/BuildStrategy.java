package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
import com.optmizer.summonerwaroptimizer.model.rune.RuneSet;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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
}
