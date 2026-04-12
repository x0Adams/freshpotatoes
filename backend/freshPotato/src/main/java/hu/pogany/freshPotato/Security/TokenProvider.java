package hu.pogany.freshPotato.Security;

import hu.pogany.freshPotato.entity.User;

public interface TokenProvider {
    String issueToken(User user);
}
