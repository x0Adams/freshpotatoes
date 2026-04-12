package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.entity.User;
import hu.pogany.freshPotato.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    public UserService(UserRepository userRepository, AuthorityService authorityService) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
    }

    public int getIdByUserName(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty())
            throw new EntityNotFoundException("no user with this name");

        return user.get().getId();

    }

    public void save(User user) {
        if (isNewUserValid(user.getUsername(), user.getEmail())) {
            userRepository.save(user);
            authorityService.saveAsUser(user.getUsername());
        } else {
            throw new EntityExistsException("username or email is already used");
        }
    }

    public void saveAsAdmin(User user) {
        save(user);
        authorityService.saveAsAdmin(user.getUsername());
    }

    public boolean isNewUserValid(String username, String email) {
        return !userRepository.existsByUsername(username) && !userRepository.existsByEmail(email);
    }


}
