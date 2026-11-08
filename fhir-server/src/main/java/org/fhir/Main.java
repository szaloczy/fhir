package org.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.HttpClientUtil;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Main {

        private static final String fhirServer = "https://launch.smarthealthit.org/v/r4/sim/WzIsIiIsImY0YmY1MjY1LThkOGUtNDA1NS1hNGU1LWIzNDg1M2YxMDk5ZiIsIkFVVE8iLDAsMCwwLCIiLCIiLCIiLCIiLCIiLCIiLCIiLDAsMSwiIl0/fhir/";

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

            String authorizeUrl = "";

            try {
                Optional<FhirUtils.SmartUrls> smartUrl = FhirUtils.tryGetSmartUrls(fhirClient);


                if (smartUrl.isPresent()) {
                    System.out.println("SMART endpoints found !");
                    authorizeUrl = smartUrl.get().authorizeUrl();

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

//            String url = authorizeUrl +
//                    "?response_type=code" +
//                    "client_id=app-client-id&" +
//                    "&redirect_uri" = +HttpUtility.UrlEncode("http://192.168.0.21/8080") +
//                    "&scope="+ HttpUtility.UrlEncode("openid+fhirUser+profile+launch/patient+patient/*.read") +
//                    "&state=local_state" +
//                    "&aud="+fhirServer;

            String redirectUri = "http://192.168.0.21:8080";
            String scope = "openid fhirUser profile launch/patient patient/*.read";

            String audience = fhirServer.endsWith("/")
                    ? fhirServer.substring(0, fhirServer.length() - 1)
                    : fhirServer;

            String url = authorizeUrl
                    + "?response_type=code"
                    + "&client_id=app-client-id"
                    + "&redirect_uri=" + URLEncoder.encode(
                    redirectUri, StandardCharsets.UTF_8)
                    + "&scope=" + URLEncoder.encode(
                    scope, StandardCharsets.UTF_8)
                    + "&state=local_state"
                    + "&aud=" + URLEncoder.encode(
                    audience, StandardCharsets.UTF_8);

            launchUrl(url);
            System.out.println();
        }

        public static void setAuthCode(String code, String state) {
            System.out.println("Code: " + code);
            System.out.println("state? " + state);
        }

    /**
     * Launch a url the use's default web browser
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
