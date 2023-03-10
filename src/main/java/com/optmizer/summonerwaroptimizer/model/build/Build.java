package com.optmizer.summonerwaroptimizer.model.build;

import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Build {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rune> runes;

    private BigDecimal efficiency;

}
