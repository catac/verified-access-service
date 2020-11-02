package net.catac.vatest;

import java.util.Map;

public class ChallengeResponseRequest {
    private Map<String, String> challengeResponse;
    private String expectedIdentity;

    public Map<String, String> getChallengeResponse() {
        return challengeResponse;
    }

    public void setChallengeResponse(Map<String, String> challengeResponse) {
        this.challengeResponse = challengeResponse;
    }

    public String getExpectedIdentity() {
        return expectedIdentity;
    }

    public void setExpectedIdentity(String expectedIdentity) {
        this.expectedIdentity = expectedIdentity;
    }
}
