package com.cmc.service.resource;

import com.cmc.dto.ResourceDTO;

public interface ResourceService {
    ResourceDTO getResources(Object data);
    long getLatestDataDate();
}
