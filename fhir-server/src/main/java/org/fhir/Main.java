package org.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

import javax.swing.text.html.Option;
import java.util.Optional;

public class Main {

        private static final String fhirServer = "https://launch.smarthealthit.org/v/r4/sim/WzIsIiIsImY0YmY1MjY1LThkOGUtNDA1NS1hNGU1LWIzNDg1M2YxMDk5ZiIsIkFVVE8iLDAsMCwwLCIiLCIiLCIiLCIiLCIiLCIiLCIiLDAsMSwiIl0/fhir/";

        public static void main(String[] args) {

            //initialize FHIR R4 context
            FhirContext ctx = FhirContext.forR4();

            //Create a client for the remote FHIR server
            IGenericClient fhirClient = ctx.newRestfulGenericClient(fhirServer);

            System.out.println("Fhir server: " + fhirServer);

            try {
                Optional<FhirUtils.SmartUrls> smartUrl = FhirUtils.tryGetSmartUrls(fhirClient);

                if (smartUrl.isPresent()) {
                    System.out.println("SMART endpoints found !");

                    System.out.println(
                            "Authorization URL: " +
                                    smartUrl.get().authorizeUrl()
                    );

                    System.out.println(
                            "Tokan URL: " +
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


        }
}
