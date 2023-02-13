package com.optmizer.summonerwaroptimizer.model.monster;

import com.optmizer.summonerwaroptimizer.model.rune.Rune;
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
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "monster", cascade = CascadeType.ALL)
    private List<Rune> runes;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private BaseMonster baseMonster;

    private Long swarfarmId;
    private Integer level;
    private Integer grade;

}
