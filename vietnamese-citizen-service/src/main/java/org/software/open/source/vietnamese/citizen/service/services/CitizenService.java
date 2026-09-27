package org.software.open.source.vietnamese.citizen.service.services;

import org.software.open.source.vietnamese.citizen.service.entries.models.requests.CitizenRequest;
import org.software.open.source.vietnamese.citizen.service.entries.models.responses.CitizenResponse;

public interface CitizenService {

  CitizenResponse createCitizen(CitizenRequest request);

}
