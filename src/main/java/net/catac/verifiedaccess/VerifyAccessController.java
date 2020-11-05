package net.catac.verifiedaccess;

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
import org.springframework.beans.factory.annotation.Value;
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
    private static final Logger logger = LoggerFactory.getLogger(VerifyAccessController.class);

    @Value("${verifiedaccess.clientSecretFile}")
    private String clientSecretFile = "/Users/ccirstoiu/Downloads/cvaa-server-test-34757cfc2b28.json";

    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private HttpRequestInitializer requestInitializer;

    // https://cloud.google.com/storage/docs/authentication#generating-a-private-key

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
                .setApplicationName("VA-Service/0.1")
                .build();

        VerifyChallengeResponseResult result = va.challenge()
                .verify(request)
                .execute();
        logger.info("Got: " + result.toPrettyString());
        return result;
    }

    @CrossOrigin
    @PostMapping("/authenticate")
    public VerifyChallengeResponseResult authenticate(@RequestBody AuthenticateRequest authenticateRequest) throws Exception {
        Map<String, String> challengeResponse = authenticateRequest.getChallengeResponse();
        SignedData sd = new SignedData()
                .setData(challengeResponse.get("data"))
                .setSignature(challengeResponse.get("signature"));
        VerifyChallengeResponseRequest vcrr = new VerifyChallengeResponseRequest()
                .setChallengeResponse(sd)
                .setExpectedIdentity(authenticateRequest.getExpectedIdentity());
        vcrr.setFactory(jsonFactory);
        return call(vcrr);
    }
}
