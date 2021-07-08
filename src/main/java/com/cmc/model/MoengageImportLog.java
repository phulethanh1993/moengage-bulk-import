package com.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "ImportDataLog")
public class MoengageImportLog {

    @Transient
    public static final String SEQUENCE_NAME = "customer_sequence";

    @Id
    private int id;
    @NotBlank
    private String date;
    @NotBlank
    private String status;
    @NotBlank
    private JSONObject dataImport;
    @NotBlank
    private List<JSONObject> importedUsers;

    public MoengageImportLog(String currentDate, String status, JSONObject dataImport, List<JSONObject> importedUsersInfo) {
        this.date = currentDate;
        this.status = status;
        this.dataImport = dataImport;
        this.importedUsers = importedUsersInfo;
    }
}
