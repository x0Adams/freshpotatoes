package hu.pogany.freshPotato.security;

import hu.pogany.freshPotato.entity.User;

public interface TokenProvider {
    String issueToken(User user);
}
