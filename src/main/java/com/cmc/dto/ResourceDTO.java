package com.cmc.dto;

import lombok.Data;
import org.json.JSONObject;

import java.util.List;

public interface ResourceDTO {
    String sources = null;
    List<JSONObject> users = null;
    List<JSONObject> devices = null;
    List<JSONObject> actions = null;
}
