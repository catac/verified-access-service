package net.catac.vatest;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.verifiedaccess.v1.Verifiedaccess;
import com.google.api.services.verifiedaccess.v1.model.SignedData;
import com.google.api.services.verifiedaccess.v1.model.VerifyChallengeResponseRequest;
import com.google.api.services.verifiedaccess.v1.model.VerifyChallengeResponseResult;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;

@RestController
public class VerifyAccessController {
    Logger logger = LoggerFactory.getLogger(VerifyAccessController.class);

    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private HttpRequestInitializer requestInitializer;

    // https://cloud.google.com/storage/docs/authentication#generating-a-private-key
    private final String clientSecretFile = "/Users/ccirstoiu/Downloads/cvaa-server-test-34757cfc2b28.json";

    @PostConstruct
    private void setup() throws IOException, GeneralSecurityException {
        logger.info("Setting up httpTransport, jsonFactory and Google Credentials ...");
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(new File(clientSecretFile)))
                .createScoped(
                        Arrays.<String>asList("https://www.googleapis.com/auth/verifiedaccess"));
        credentials.refreshIfExpired();
        this.requestInitializer = new HttpCredentialsAdapter(credentials);
    }

    private VerifyChallengeResponseResult call(VerifyChallengeResponseRequest request) throws GeneralSecurityException, IOException {
        logger.info("Calling with: " + request.toPrettyString());
        Verifiedaccess va = new Verifiedaccess
                .Builder(httpTransport, jsonFactory, requestInitializer)
                .setApplicationName("VA-Test/0.1")
                .build();

        VerifyChallengeResponseResult result = va.challenge()
                .verify(request)
                .execute();
        logger.info("Got: " + result.toPrettyString());
        return result;
    }

    /**
     * Invokes the synchronous RPC call that verifies the device response.
     * Returns the result protobuf as a string.
     *
     * @param signedData base64 encoded signedData blob (this is a response from device)
     * @param expectedIdentity expected identity (domain name or user email)
     * @return the verification result protobuf as string
     */
    private VerifyChallengeResponseRequest newVerificationRequest(
            String signedData, String expectedIdentity) throws IOException {
        SignedData sd = new SignedData()
                .setData(signedData);
        VerifyChallengeResponseRequest request = new VerifyChallengeResponseRequest()
            .setChallengeResponse(sd)
            .setExpectedIdentity(expectedIdentity == null ? "" : expectedIdentity);
        return request;
    }
    
    @CrossOrigin
    @PostMapping("/challengeResponseRequest")
    public VerifyChallengeResponseResult challenge(@RequestBody ChallengeResponseRequest challengeResponseRequest) throws Exception {
        Map<String, String> challengeResponse = challengeResponseRequest.getChallengeResponse();
        SignedData sd = new SignedData()
                .setData(challengeResponse.get("data"))
                .setSignature(challengeResponse.get("signature"));
        VerifyChallengeResponseRequest vcrr = new VerifyChallengeResponseRequest()
                .setChallengeResponse(sd)
                .setExpectedIdentity(challengeResponseRequest.getExpectedIdentity());
        return call(vcrr);
    }

    public static void main(String[] args) throws Exception {
        VerifyAccessController vac = new VerifyAccessController();
        vac.setup();

        String signedData = "";
        String expectedIdentity = "";
        VerifyChallengeResponseRequest vcrr = vac.newVerificationRequest(signedData, expectedIdentity);
        vac.call(vcrr);
    }
}
