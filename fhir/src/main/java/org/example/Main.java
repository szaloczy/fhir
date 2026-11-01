package org.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

public class Main {

    static void main() {
        String fhirServer = "https://server.fire.ly";
        FhirContext ctx = FhirContext.forR4();

        IGenericClient client = ctx.newRestfulGenericClient(fhirServer);

        Bundle patientBundle = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("Doe"))
                .returnBundle(Bundle.class)
                .execute();

        System.out.println();

        int patientNumber = 0;


        int pageNumber = 1;
        int totalProcessed = 0;

        // 2. Process pages until there are no more
        while (patientBundle != null) {

            System.out.println("\n--- Page " + pageNumber + " ---");

            // 3. Process every patient on the current page
            for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {

                if (entry.getResource() instanceof Patient patient) {

                    totalProcessed++;

                    System.out.println("Patient ID: "
                            + patient.getIdElement().getIdPart());

                    if (patient.hasName()) {
                        System.out.println("Name: "
                                + patient.getNameFirstRep().getNameAsSingleString());
                    }

                    System.out.println("-------------------");
                }
            }

            // 4. Check if another page exists
            if (patientBundle.getLink(Bundle.LINK_NEXT) != null) {

                // 5. Load the next page
                patientBundle = client.loadPage()
                        .next(patientBundle)
                        .execute();

                pageNumber++;

            } else {
                patientBundle = null;
            }
        }

        System.out.println("\nTotal processed: " + totalProcessed);
    }


}
