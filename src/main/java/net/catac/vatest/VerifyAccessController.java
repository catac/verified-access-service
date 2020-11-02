package net.catac.vatest;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.verifiedaccess.v1.Verifiedaccess;
import com.google.api.services.verifiedaccess.v1.model.SignedData;
import com.google.api.services.verifiedaccess.v1.model.VerifyChallengeResponseRequest;
import com.google.api.services.verifiedaccess.v1.model.VerifyChallengeResponseResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.collect.Lists;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.auth.ClientAuthInterceptor;

import javax.net.ssl.SSLException;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;


public class VerifyAccessController {

    private NetHttpTransport httpTransport;
    private JsonFactory jsonFactory;

    // https://cloud.google.com/storage/docs/authentication#generating-a-private-key
    private final String clientSecretFile = "/Users/catac/Downloads/api-project-712859566944-5b796ce5f681.json";

    private void setup() throws IOException, SSLException, GeneralSecurityException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.jsonFactory = JacksonFactory.getDefaultInstance();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(new FileInputStream(new File(clientSecretFile))));

        List<ClientInterceptor> interceptors = Lists.newArrayList();
        // Attach a credential for my service account and scope it for the API.
        GoogleCredentials credentials =
                ServiceAccountCredentials.class.cast(
                        GoogleCredentials.fromStream(
                                new FileInputStream(new File(clientSecretFile))));
        credentials = credentials.createScoped(
                Arrays.<String>asList("https://www.googleapis.com/auth/verifiedaccess"));
        interceptors.add(
                new ClientAuthInterceptor(credentials, Executors.newSingleThreadExecutor()));

        // Create a stub bound to the channel with the interceptors applied
        blockingStub = VerifiedAccessGrpc.newBlockingStub(
                ClientInterceptors.intercept(channel, interceptors));
    }

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(new FileInputStream(new File(clientSecretFile))));

        Collection<String> scopes = Arrays.asList(
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/userinfo.profile");
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    /**
     * Invokes the synchronous RPC call that verifies the device response.
     * Returns the result protobuf as a string.
     *
     * @param signedData base64 encoded signedData blob (this is a response from device)
     * @param expectedIdentity expected identity (domain name or user email)
     * @return the verification result protobuf as string
     */
    private VerifyChallengeResponseResult call(VerifyChallengeResponseRequest request) throws GeneralSecurityException, IOException {
        Credential credential = null;
        System.out.println("Calling with: " + request);
        Verifiedaccess va = new Verifiedaccess
                .Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("VA-Test/0.1")
                .build();

        VerifyChallengeResponseResult result = va.challenge()
                .verify(request)
                .execute();
        System.out.println("Got: " + result);
        return result;
    }

    private VerifyChallengeResponseRequest newVerificationRequest(
            String signedData, String expectedIdentity) throws IOException {
        SignedData sd = new SignedData()
                .setData(signedData);
        VerifyChallengeResponseRequest request = new VerifyChallengeResponseRequest()
            .setChallengeResponse(sd)
            .setExpectedIdentity(expectedIdentity == null ? "" : expectedIdentity);
        return request;
    }

}
