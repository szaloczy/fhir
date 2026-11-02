package org.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import java.util.*;

public class Main {

    static void main() {
        final Dictionary<String, String> _fhirServers = new Hashtable<>() {{
            put("publicFire-ly", "https://server.fire.ly");
            put("PublicHAPI", "http://hapi.fhir.org/baseR4/");
            put("Local", "http://localhost:8080/fhir");
        }};

        String fhirServer = _fhirServers.get("Local");
        FhirContext ctx = FhirContext.forR4();
        IGenericClient client = ctx.newRestfulGenericClient(fhirServer);

        List<Patient> patients = getPatients(client, null, 20, false);
        createPatient(client, "Jhon", "Doe");

    }

    /**
     * Get a list of patients matching the specified criteria
     * @param fhirClient
     * @param patientCriteria
     * @param maxPatients Maximum number of the patients to return
     * @param onlyWithEncounters Flag to only return patients with encounters
     *
     * @return List<Patient>
     * **/
    private static List<Patient> getPatients(IGenericClient fhirClient, String[] patientCriteria, int maxPatients, boolean onlyWithEncounters) {

        List<Patient> patients = new ArrayList<>();
        patientCriteria = null;
        maxPatients = 20;
        onlyWithEncounters = false;

        Bundle patientBundle;

            if (patientCriteria == null || patientCriteria.length == 0) {
                patientBundle = fhirClient.search()
                            .forResource(Patient.class)
                            .returnBundle(Bundle.class)
                            .execute();
            } else {
                patientBundle = fhirClient.search()
                        .forResource(Patient.class)
                        .where(Patient.FAMILY.matches().value(Arrays.toString(patientCriteria)))
                        .returnBundle(Bundle.class)
                        .execute();
            }


        System.out.println();

        int pageNumber = 1;
        int totalProcessed = 0;

        // 2. Process pages until there are no more
        while (patientBundle != null) {

            System.out.println("Patient bundle total: " + patientBundle.getTotal() + " Entry count: " + patientBundle.getEntry().size());

            // 3. Process every patient on the current page
            for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {

                if (entry.getResource() instanceof Patient patient && entry.getResource() != null) {

                    Bundle encounterBundle = fhirClient.search().forResource(Encounter.class).returnBundle(Bundle.class).execute();

                    totalProcessed++;

                    System.out.println("Patient ID: " + patient.getIdElement().getIdPart());

                    if (onlyWithEncounters && (encounterBundle.getTotal() == 0)) {
                        continue;
                    }

                    patients.add(patient);

                    System.out.println("Entry: " + patients.size() + ": " + entry.getFullUrl());
                    System.out.println(" -   Id: " + patient.getId());

                    if (patient.hasName()) {
                        System.out.println("Name: "+ patient.getNameFirstRep().getNameAsSingleString());
                    }

                    if (encounterBundle.getTotal() > 0) {
                        System.out.println(" - Encounters Total: " + encounterBundle.getTotal() + " Entry count: " + encounterBundle.getEntry().size());
                    }

                    System.out.println("-------------------");
                }

                if (patients.size() >= maxPatients) {
                    break;
                }
            }

            // 4. Check if another page exists
            if (patientBundle.getLink(Bundle.LINK_NEXT) != null) {

                // 5. Load the next page
                patientBundle = fhirClient.loadPage()
                        .next(patientBundle)
                        .execute();

                pageNumber++;

            } else {
                patientBundle = null;
            }
        }
        System.out.println("\nTotal processed: " + totalProcessed);

        return patients;
    }

    /**
     *
     * @param fhirClient
     * @param familyName
     * @param giveName
     */
    private static void createPatient(IGenericClient fhirClient, String familyName, String giveName) {
        Patient toCreate = new Patient();

        HumanName name = new HumanName();
        name.setFamily(familyName);
        name.addGiven(giveName);

        toCreate.addName(name);

        fhirClient.create().resource(toCreate).execute();
    }
}
