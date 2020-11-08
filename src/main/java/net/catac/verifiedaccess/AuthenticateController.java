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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;

@RestController
public class AuthenticateController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticateController.class);

    @Autowired
    private CAHelper caHelper;

    @Value("${verifiedaccess.googleCredentialsFile}")
    private String googleCredentialsFile;

    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory;
    private HttpRequestInitializer requestInitializer;

    @PostConstruct
    private void setup() throws IOException, GeneralSecurityException {
        logger.info("Setting up httpTransport, jsonFactory and Google Credentials ...");
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(new File(googleCredentialsFile)))
                .createScoped(
                        Arrays.<String>asList("https://www.googleapis.com/auth/verifiedaccess"));
        credentials.refreshIfExpired();
        this.requestInitializer = new HttpCredentialsAdapter(credentials);
    }

    @CrossOrigin
    @PostMapping("/authenticate")
    public AuthenticateResponse authenticate(@RequestBody AuthenticateRequest authenticateRequest) throws Exception {
        VerifyChallengeResponseRequest apiRequest = buildApiRequest(authenticateRequest);

        VerifyChallengeResponseResult apiResponse = callGoogleAPI(apiRequest);

        String pskac = apiResponse.getSignedPublicKeyAndChallenge();
        String identity = apiRequest.getExpectedIdentity();
        String certificateDerB64 = signCertificate(pskac, identity);

        AuthenticateResponse authenticateResponse = new AuthenticateResponse()
                .setIdentity(identity)
                .setCertificateDerB64(certificateDerB64);
        return authenticateResponse;        
    }

    private VerifyChallengeResponseRequest buildApiRequest(AuthenticateRequest authenticateRequest){
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

    private VerifyChallengeResponseResult callGoogleAPI(VerifyChallengeResponseRequest request) throws GeneralSecurityException, IOException {
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

    private String signCertificate(String pskac, String identity) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("SPKAC=").append(pskac).append("\n");
        sb.append("CN=").append(identity).append("\n");

        return caHelper.runCAScript("sign.sh", sb.toString());
    }

    @PostMapping("/sign/{identity}")
    public String signPSKAC(@RequestBody String pskac, @PathVariable("identity") String identity) throws Exception {
        return signCertificate(pskac, identity);
    }
}
