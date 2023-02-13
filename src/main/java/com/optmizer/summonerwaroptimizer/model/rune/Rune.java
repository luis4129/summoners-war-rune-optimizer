package com.optmizer.summonerwaroptimizer.model.rune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.Monster;
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
public class Rune {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long swarfarmId;

    @ManyToOne
    @JsonIgnore
    private Monster monster;

    @OneToOne(cascade = CascadeType.ALL)
    private PrefixStat prefixStat;

    @OneToMany(mappedBy = "rune", cascade = CascadeType.ALL)
    private List<SubStat> subStats;

    @Column(name = "rune_set")
    private RuneSet set;

    private Attribute mainStat;
    private Integer slot;
    private Integer grade;
    private Integer level;


}
