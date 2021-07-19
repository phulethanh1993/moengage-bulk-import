package com.cmc.utils;

import com.cmc.model.ImportedUser;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CustomerUtils {
    public long findLatestDataDate(List<ImportedUser> importedUsers) {
        long latestDataDate = 0;
        if (importedUsers == null || importedUsers.size() == 0) {
            return latestDataDate;
        }
        for (ImportedUser user : importedUsers) {
            latestDataDate = Math.max(latestDataDate, user.getUpdatedTime());
        }
        return latestDataDate;
    }
}
