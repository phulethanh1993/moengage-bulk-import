package com.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotBlank;

@Data
@Document(collection = "ImportDataLog")
public class Log {
    @Id
    private String id;
    @NotBlank
    private String date;
    @NotBlank
    private String status;
    @NotBlank
    private JSONObject dataImport;

    public Log(String date, String status, JSONObject dataImport) {
        this.id = new ObjectId().toHexString();
        this.date = date;
        this.status = status;
        this.dataImport = dataImport;
    }
}
