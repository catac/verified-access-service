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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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
public class AuthenticateController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticateController.class);

    @Autowired
    private ProcessHelper processHelper;

    @Value("${verifiedaccess.googleCredentialsFile}")
    private String googleCredentialsFile;

    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private HttpRequestInitializer requestInitializer;

    @PostConstruct
    private void setup() throws IOException, GeneralSecurityException {
        logger.info("Setting up httpTransport, jsonFactory ...");
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        logger.info("Loading Google Credentials from " + googleCredentialsFile);
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(new File(googleCredentialsFile)))
                .createScoped(
                        Arrays.asList("https://www.googleapis.com/auth/verifiedaccess"));
        credentials.refreshIfExpired();
        this.requestInitializer = new HttpCredentialsAdapter(credentials);
    }

    @CrossOrigin
    @PostMapping("/authenticate")
    public AuthenticateResponse authenticate(@RequestBody AuthenticateRequest authenticateRequest) throws Exception {
        VerifyChallengeResponseRequest apiRequest = buildApiRequest(authenticateRequest);

        VerifyChallengeResponseResult apiResponse = callGoogleAPI(apiRequest);

        String spkac = apiResponse.getSignedPublicKeyAndChallenge();
        String identity = apiRequest.getExpectedIdentity();
        String certificateDerB64 = signCertificate(spkac, identity);

        return buildAuthenticateResponse(identity, certificateDerB64);
    }

    private AuthenticateResponse buildAuthenticateResponse(String identity, String certificateDerB64) {
        return new AuthenticateResponse()
                .setIdentity(identity)
                .setCertificateDerB64(certificateDerB64);
    }

    private VerifyChallengeResponseRequest buildApiRequest(AuthenticateRequest authenticateRequest) {
        Map<String, String> challengeResponse = authenticateRequest.getChallengeResponse();
        SignedData sd = new SignedData()
                .setData(challengeResponse.get("data"))
                .setSignature(challengeResponse.get("signature"));
        VerifyChallengeResponseRequest apiRequest = new VerifyChallengeResponseRequest()
                .setChallengeResponse(sd)
                .setExpectedIdentity(authenticateRequest.getExpectedIdentity());
        apiRequest.setFactory(jsonFactory);
        return apiRequest;
    }

    private VerifyChallengeResponseResult callGoogleAPI(VerifyChallengeResponseRequest request) throws IOException {
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

    private String signCertificate(String spkac, String identity) throws Exception {
        String sb = "SPKAC=" + spkac + "\n" +
                "CN=" + identity + "\n";
        return processHelper.runCAScript("ca/sign.sh", sb);
    }

    @PostMapping("/sign/{identity}")
    public AuthenticateResponse signPSKAC(@RequestBody VerifyChallengeResponseResult result, @PathVariable("identity") String identity) throws Exception {
        String certificateDerB64 = signCertificate(result.getSignedPublicKeyAndChallenge(), identity);
        return buildAuthenticateResponse(identity, certificateDerB64);
    }
}
