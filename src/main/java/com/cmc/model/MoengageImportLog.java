package com.cmc.model;

import lombok.Data;
import lombok.NoArgsConstructor;
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
    private Integer id;
    @NotBlank
    private String importDate;
    @NotBlank
    private long dataDate;
    @NotBlank
    private String status;
    @NotBlank
    private List<ImportedUser> importedUsers;

    public MoengageImportLog(String importDate, long dataDate, String status, List<ImportedUser> importedUsersInfo) {
        this.importDate = importDate;
        this.dataDate = dataDate;
        this.status = status;
        this.importedUsers = importedUsersInfo;
    }
}
