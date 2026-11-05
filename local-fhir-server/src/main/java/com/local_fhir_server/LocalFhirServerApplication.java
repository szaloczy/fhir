package com.local_fhir_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class LocalFhirServerApplication {

    private static final String _defaultFhirServer = "https://launch.smarthealthit.org/v/r4/sim/WzIsIiIsImY0YmY1MjY1LThkOGUtNDA1NS1hNGU1LWIzNDg1M2YxMDk5ZiIsIkFVVE8iLDAsMCwwLCIiLCIiLCIiLCIiLCIiLCIiLCIiLDAsMSwiIl0/fhir/";

	public static void main(String[] args) {
		SpringApplication.run(LocalFhirServerApplication.class, args);

        System.out.println("FHIR server: " + _defaultFhirServer);

        IGenericClient v;

	}

}
