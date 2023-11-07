package com.github.StasMalykhin.aviabot.repository;

import com.github.StasMalykhin.aviabot.entity.AirTicketSearch;
import com.github.StasMalykhin.aviabot.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author Stanislav Malykhin
 */
public interface AirTicketSearchRepository extends JpaRepository<AirTicketSearch, Long> {
    Optional<AirTicketSearch> findByAppUser(AppUser appUser);
}
