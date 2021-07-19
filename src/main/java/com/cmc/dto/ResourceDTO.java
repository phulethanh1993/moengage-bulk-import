package com.cmc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ResourceDTO implements Serializable {
    Map<String, List<JSONObject>> dataImport;
}
