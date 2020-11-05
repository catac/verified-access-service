# verified-access-service

This is a TEST implementation of the "Network Service" in the
[Chrome Verified Access Developer's Guide](https://developers.google.com/chrome/verified-access/developer-guide).

## Some links
- https://developers.google.com/chrome/verified-access/overview?hl=en_US
- https://cloud.google.com/storage/docs/authentication#generating-a-private-key
- https://cloud.google.com/iam/docs/creating-managing-service-account-keys
- https://console.cloud.google.com/
- https://github.com/googleapis/google-api-java-client-services#supported-google-apis
- https://github.com/googleapis/google-api-java-client
- https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-verifiedaccess/v1
- https://developers.google.com/api-client-library/java/google-api-java-client/oauth2
- https://github.com/googleapis/google-auth-library-java

## Tests

```
curl http://localhost:8080/
curl -v -H 'Content-type:application/json' -X POST -d@test-verify.json localhost:8080/authenticate
```
