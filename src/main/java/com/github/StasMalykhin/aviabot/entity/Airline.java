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
@Table(name = "airline")
public class Airline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String airlineCode;
    private String name;
    private String nameTranslations;
}
