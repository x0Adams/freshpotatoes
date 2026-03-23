package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public int getIdByUserName(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty())
            throw new EntityNotFoundException("no user with this name");

        return user.get().getId();

    }
}
