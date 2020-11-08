package net.catac.verifiedaccess;

public class AuthenticateResponse {
    private String identity;
    private String certificateDerB64;

    public String getIdentity() {
        return identity;
    }

    public AuthenticateResponse setIdentity(String identity) {
        this.identity = identity;
        return this;
    }

    public String getCertificateDerB64() {
        return certificateDerB64;
    }

    public AuthenticateResponse setCertificateDerB64(String certificateDerB64) {
        this.certificateDerB64 = certificateDerB64;
        return this;
    }
}
