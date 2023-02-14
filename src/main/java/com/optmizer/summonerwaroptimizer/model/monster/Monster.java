package com.optmizer.summonerwaroptimizer.model.monster;

import com.optmizer.summonerwaroptimizer.model.build.Build;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Build build;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private BaseMonster baseMonster;

    private Long swarfarmId;
    private Integer level;
    private Integer grade;

}
