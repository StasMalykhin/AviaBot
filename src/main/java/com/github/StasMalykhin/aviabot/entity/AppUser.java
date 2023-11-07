package com.github.StasMalykhin.aviabot.entity;

import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "app_user")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long telegramUserId;
    @CreationTimestamp
    private LocalDateTime firstLoginDate;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    @Enumerated(EnumType.STRING)
    private UserState state;
    @OneToOne(mappedBy = "appUser")
    private AirTicketSearch airTicketSearch;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "appUser")
    private List<AirTicket> airTickets = new ArrayList<>();

    public void addAirTicket(AirTicket airTicket) {
        airTickets.add(airTicket);
        airTicket.setAppUser(this);
    }

    public void removeAirTicket(AirTicket airTicket) {
        airTickets.remove(airTicket);
        airTicket.setAppUser(null);
    }
}
