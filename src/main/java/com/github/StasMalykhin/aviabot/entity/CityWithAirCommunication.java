package com.github.StasMalykhin.aviabot.entity;

import lombok.*;

/**
 * @author Stanislav Malykhin
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CityWithAirCommunication {
    private String codeCity;
    private String nameCity;
    private String nameCountry;
}
