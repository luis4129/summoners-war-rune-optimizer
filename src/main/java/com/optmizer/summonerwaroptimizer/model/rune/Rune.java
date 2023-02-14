package com.optmizer.summonerwaroptimizer.model.rune;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.build.Build;
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
    private Build build;

    @OneToOne(cascade = CascadeType.ALL)
    private MainStat mainStat;

    @OneToOne(cascade = CascadeType.ALL)
    private PrefixStat prefixStat;

    @OneToMany(mappedBy = "rune", cascade = CascadeType.ALL)
    private List<SubStat> subStats;

    @Column(name = "rune_set")
    private RuneSet set;

    private Integer slot;
    private Integer grade;
    private Integer level;


}
