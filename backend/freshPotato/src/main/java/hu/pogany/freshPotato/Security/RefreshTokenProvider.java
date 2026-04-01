package hu.pogany.freshPotato.Security;

public interface RefreshTokenProvider extends TokenProvider{
    String getBase64Token(byte[] token);
    String hashToken(String token);
    boolean isHashValid(String token, String hash);
    byte[] getToken();
}
