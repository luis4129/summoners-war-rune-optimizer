package com.optmizer.summonerwaroptimizer.model.optimizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.optmizer.summonerwaroptimizer.model.monster.MonsterAttribute;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private BuildStrategy buildStrategy;

    private BuildPreferenceType type;
    private MonsterAttribute attribute;

    private Integer minimumValue;
    private Integer maximumValue;

    public Integer getThresholdValue() {
        return switch (type) {
            case AS_HIGH_AS_POSSIBLE -> null;
            case WITHIN_REQUIRED_RANGE -> maximumValue;
            case ONLY_REQUIRED_VALUE -> minimumValue;
        };
    }


}
