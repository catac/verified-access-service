# verified-access-service

This is a TEST implementation of the "Network Service" in the
[Chrome Verified Access Developer's Guide](https://developers.google.com/chrome/verified-access/developer-guide).

## Setup

- Login into the Google Cloud console: https://console.cloud.google.com/
- Create an API Key and a Service Account under API & Services -> Credentials
- Under the Service Account, create a key and download the json file
- The API Key should be used on the Chromebook to generate the challenge
- The json credentials file associated to the Service Account will be used by this service

- Login into the Google Admin console: https://admin.google.com/
- Go to Devices -> Chrome -> Settings for your organization
- Under User & Browser Settings -> User verification -> Verified Mode, allow the
  service account email address configured above to receive user data
- Under User & Browser Settings -> Device Settings -> Enrollment and Access -> Verified mode,
  allow full access to the same service account email address. 

- Initialize the local CA, go into the `ca` subdir and run `./init.sh`

## Tests

```
mvn package
java -jar target/verified-access-service-*.jar
...

curl http://localhost:8080/
curl -v -H 'Content-type:application/json' -X POST -d@test-verify.json localhost:8080/authenticate
```

## Additional links
- https://developers.google.com/chrome/verified-access/overview?hl=en_US
- https://cloud.google.com/storage/docs/authentication#generating-a-private-key
- https://cloud.google.com/iam/docs/creating-managing-service-account-keys
- https://console.cloud.google.com/
- https://github.com/googleapis/google-api-java-client-services#supported-google-apis
- https://github.com/googleapis/google-api-java-client
- https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-verifiedaccess/v1
- https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
- https://github.com/googleapis/google-auth-library-java
- https://developers.google.com/chrome/verified-access/reference/rest/v1/challenge/verify
