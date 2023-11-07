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
@Table(name = "city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cityCode;
    private String countryCode;
    private String name;
    private String nameTranslations;
}
