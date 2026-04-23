package hu.pogany.freshPotato.service;

import hu.pogany.freshPotato.entity.Authorities;
import hu.pogany.freshPotato.entity.Authority;
import hu.pogany.freshPotato.entity.AuthorityId;
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

    public void saveAsUser(String username) {
        Authority authority = getAuthority(username, Authorities.ROLE_USER);

        authorityRepository.save(authority);
    }

    public void saveAsAdmin(String username) {
        Authority authority = getAuthority(username, Authorities.ROLE_ADMIN);

        authorityRepository.save(authority);
    }

    private Authority getAuthority(String username, Authorities authorities) {
        AuthorityId id = new AuthorityId();
        id.setAuthority(authorities);
        id.setUsername(username);

        Authority authority = new Authority();
        authority.setId(id);

        return authority;
    }
}
