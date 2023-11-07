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
@Table(name = "air_ticket_search")
public class AirTicketSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String departureCityCode;
    private String destinationCityCode;
    private String departureAt;
    @OneToOne()
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser appUser;
}
