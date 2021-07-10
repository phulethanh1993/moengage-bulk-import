package com.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportedUser {
    @NotBlank
    private long updatedTime;
    @NotBlank
    private String customerId;
}
