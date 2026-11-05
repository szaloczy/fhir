package org.fhir;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import java.util.Optional;

public class FhirUtils {

    private static final String SMART_EXTENSION_URL =
            "http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris";

    public record SmartUrls(
            String authorizeUrl,
            String tokenUrl
    ) {}

    public static Optional<SmartUrls> tryGetSmartUrls(IGenericClient fhirClient) {

        CapabilityStatement capabilities = fhirClient
                .capabilities()
                .ofType(CapabilityStatement.class)
                .execute();

        String authorizeUrl = null;
        String tokenUrl = null;

        for (CapabilityStatement.CapabilityStatementRestComponent comp : capabilities.getRest()) {
            if (!comp.hasSecurity()) {
                continue;
            }

            for (Extension securityExt : comp.getSecurity().getExtension()) {
                if (!SMART_EXTENSION_URL.equals(securityExt.getUrl())) {
                    continue;
                }

                for(Extension smartExt : securityExt.getExtension()) {

                    if(!(smartExt.getValue() instanceof UriType uriType)) {
                        continue;
                    }

                    switch (smartExt.getUrl()) {
                        case "authorize":
                            authorizeUrl = uriType.getValue();
                            break;
                        case "token":
                            tokenUrl = uriType.getValue();
                            break;
                    }
                }
            }
        }

        if (authorizeUrl == null || authorizeUrl.isBlank() || tokenUrl == null || tokenUrl.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new SmartUrls(authorizeUrl, tokenUrl));
    }
}
