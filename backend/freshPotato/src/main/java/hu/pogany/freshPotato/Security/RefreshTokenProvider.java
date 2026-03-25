package hu.pogany.freshPotato.Security;

public interface RefreshTokenProvider {
    String getBase64Token();
    String hashToken(String token);
    boolean isHashValid(String token, String hash);
}
