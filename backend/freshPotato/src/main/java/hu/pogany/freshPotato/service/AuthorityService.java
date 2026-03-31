package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.entity.Authorities;
import hu.pogany.freshPotato.entity.Authority;
import hu.pogany.freshPotato.repository.AuthorityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorityService {
    AuthorityRepository authorityRepository;

    public AuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    public List<String> findAuthorityByUser(String username) {
        return authorityRepository.findByIdUsername(username)
                .stream()
                .map(Authority::getAuthority)
                .map(Authorities::toString)
                .toList();
    }
}
