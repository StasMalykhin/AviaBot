package com.github.StasMalykhin.aviabot.service;

import com.github.StasMalykhin.aviabot.entity.AppUser;
import com.github.StasMalykhin.aviabot.entity.enums.UserState;
import com.github.StasMalykhin.aviabot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сохраняет, ищет пользователя, обновляет состояние пользователя.
 *
 * @author Stanislav Malykhin
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<AppUser> findUserByTelegramUserId(Long id) {
        return userRepository.findByTelegramUserId(id);
    }

    public AppUser save(AppUser user) {
        return userRepository.save(user);
    }

    public void updateStatus(AppUser user, UserState state) {
        user.setState(state);
        userRepository.save(user);
    }
}
