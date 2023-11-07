package com.github.StasMalykhin.aviabot.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author Stanislav Malykhin
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "airport")
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cityCode;
    private String countryCode;
    private String airportCode;
    private String name;
    private String nameTranslations;
}
