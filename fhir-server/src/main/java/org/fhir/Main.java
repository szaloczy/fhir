package org.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.r4.model.Patient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Main {

        private static final String fhirServer = "https://launch.smarthealthit.org/v/r4/sim/WzIsIiIsImY0YmY1MjY1LThkOGUtNDA1NS1hNGU1LWIzNDg1M2YxMDk5ZiIsIkFVVE8iLDAsMCwwLCIiLCIiLCIiLCIiLCIiLCIiLCIiLDAsMSwiIl0/fhir/";
        private static final String CLIENT_ID = "app-client-id";
        private static final String REDIRECT_URI = "http://192.168.0.21:8080";
        private static final HttpClient client = HttpClient.newHttpClient();
        private static String authorizeUrl = "";
        private static String tokenUrl = "";

        public static void main(String[] args) {

            FhirServer server = new FhirServer(8080);
            try {
                server.start();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            //initialize FHIR R4 context
            FhirContext ctx = FhirContext.forR4();

            //Create a client for the remote FHIR server
            IGenericClient fhirClient = ctx.newRestfulGenericClient(fhirServer);
            System.out.println("Fhir server: " + fhirServer);

            try {
                Optional<FhirUtils.SmartUrls> smartUrl = FhirUtils.tryGetSmartUrls(fhirClient);


                if (smartUrl.isPresent()) {
                    System.out.println("SMART endpoints found !");
                    authorizeUrl = smartUrl.get().authorizeUrl();
                    tokenUrl = smartUrl.get().tokenUrl();

                    System.out.println(
                            "Authorization URL: " +
                                    smartUrl.get().authorizeUrl()
                    );

                    System.out.println(
                            "Token URL: " +
                                    smartUrl.get().tokenUrl()
                    );
                } else {
                    System.out.println(
                            "SMART OAuth endpoints were not found."
                    );
                }
            } catch (Exception e) {
                System.err.println("FHIR request failed: " + e.getMessage());
                e.printStackTrace();
            }

            String scope = "openid fhirUser profile launch/patient patient/*.read";

            String audience = fhirServer.endsWith("/")
                    ? fhirServer.substring(0, fhirServer.length() - 1)
                    : fhirServer;

            String url = authorizeUrl
                    + "?response_type=code"
                    + "&client_id=" + CLIENT_ID
                    + "&redirect_uri=" + URLEncoder.encode(
                    REDIRECT_URI, StandardCharsets.UTF_8)
                    + "&scope=" + URLEncoder.encode(
                    scope, StandardCharsets.UTF_8)
                    + "&state=local_state"
                    + "&aud=" + URLEncoder.encode(
                    audience, StandardCharsets.UTF_8);

            launchUrl(url);
            System.out.println();
        }

        public static void setAuthCode(String code, String state) {

            if (code == null || code.isBlank()) {
                System.err.println("Authorization code is missing. Ignoring request.");
                return;
            }

            if (state == null || !state.equals("local_state")) {
                System.err.println("Invalid OAuth state");
                return;
            }
            System.out.println("Received code: " + code);

            Map<String, String> requestValues = new HashMap<>() {{
                put("grant_type","authorization_code");
                put("code", code);
                put("redirect_uri", REDIRECT_URI);
                put("client_id", CLIENT_ID);
            }};
            String formData = requestValues.entrySet()
                    .stream()
                    .map(entry ->
                            URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                                    + "=" +
                                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
                    )
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header(
                            "Content-Type",
                            "application/x-www-form-urlencoded"
                    )
                    .header("Accept","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            var response = client.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            response.thenAccept(res -> {
                System.out.println("HTTP status: " + res.statusCode());
                System.out.println("Response: ");

                try {
                    if (res.statusCode() < 200 || res.statusCode() >= 300) {
                        System.err.println("Token request failed: " + res.statusCode());
                        System.err.println(res.body());
                        return;
                    }

                    ObjectMapper objectMapper = new ObjectMapper();

                    SmartResponseDTO smartResponseDTO = objectMapper.readValue(
                            res.body(),
                            SmartResponseDTO.class
                    );

                    CompletableFuture.runAsync(() -> useToken(smartResponseDTO));

                } catch (Exception e) {
                    System.err.println("Failed to deserialize token response: " + e.getMessage());
                    e.printStackTrace();
                }

                System.out.println(res.body());
            }).exceptionally(error -> {
                System.err.println("Token request failed: " + error.getMessage());

                return null;
            });


        }

    /**
     * Use a SMART token with the FHIR Net API
     * @param smartResponseDTO
     */
    public static void useToken(SmartResponseDTO smartResponseDTO) {
        if (smartResponseDTO == null) {
            throw new IllegalArgumentException();
        }

        FhirContext ctx = new FhirContext().forR4();

        IGenericClient fhirClient = ctx.newRestfulGenericClient(fhirServer);

        BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(smartResponseDTO.accessToken());
        fhirClient.registerInterceptor(authInterceptor);

        Patient patient = fhirClient
                .read()
                .resource(Patient.class)
                .withId(smartResponseDTO
                        .patient())
                .execute();

        System.out.println("Patient: " + patient.getNameFirstRep().getNameAsSingleString());
    }

    /**
     * Launch a url the user's default web browser
     * @param url
     */
    public static void launchUrl(String url) {
        String os = System.getProperty("os.name").toLowerCase();

        ProcessBuilder processbuilder;

        if(os.contains("win")) {
            processbuilder = new ProcessBuilder(
                    "rundll32",
                    "url.dll,FileProtocolHandler",
                    url
            );
        } else if (os.contains("mac")) {
            processbuilder = new ProcessBuilder("open", url);
        } else if (os.contains("nux") || os.contains("nix")) {
            processbuilder = new ProcessBuilder("xdg-open", url);
        } else {
            throw new UnsupportedOperationException("Unsopperted operating system: " + os);
        }
        try {
            processbuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
