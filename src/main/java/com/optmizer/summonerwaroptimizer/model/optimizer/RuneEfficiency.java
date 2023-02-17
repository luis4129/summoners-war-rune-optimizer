package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.rune.Rune;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuneEfficiency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Long id;

    @ManyToOne
    private Rune rune;

    @ManyToOne
    @JsonIgnore
    private BuildStrategy buildStrategy;

    private BigDecimal efficiency;
}
