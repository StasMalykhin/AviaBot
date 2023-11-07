package com.github.StasMalykhin.aviabot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

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
@Table(name = "air_ticket")
public class AirTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String flightNumber;
    private String nameCityDeparture;
    private String codeCityDeparture;
    private String nameCityDestination;
    private String codeCityDestination;
    private String departureAirport;
    private String destinationAirport;
    private Integer firstMinPrice;
    private Integer actualMinPrice;
    private String percentageDeviation;
    private String nameAirline;
    private Date departureAt;
    private Date departureDay;
    private Date arrivalAt;
    private Integer flightTime;
    @Transient
    private String URL;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private AppUser appUser;

    public AirTicket(String flightNumber, String nameCityDeparture, String codeCityDeparture,
                     String departureAirport, String nameCityDestination, String codeCityDestination,
                     String destinationAirport, Integer firstMinPrice) {
        this.flightNumber = flightNumber;
        this.nameCityDeparture = nameCityDeparture;
        this.codeCityDeparture = codeCityDeparture;
        this.departureAirport = departureAirport;
        this.nameCityDestination = nameCityDestination;
        this.codeCityDestination = codeCityDestination;
        this.destinationAirport = destinationAirport;
        this.firstMinPrice = firstMinPrice;
    }
}
