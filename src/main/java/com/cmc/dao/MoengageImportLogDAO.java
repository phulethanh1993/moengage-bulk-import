package com.cmc.dao;

import com.cmc.model.MoengageImportLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MoengageImportLogDAO extends MongoRepository<MoengageImportLog, String> {

    MoengageImportLog findFirstByOrderByIdDesc();
}
