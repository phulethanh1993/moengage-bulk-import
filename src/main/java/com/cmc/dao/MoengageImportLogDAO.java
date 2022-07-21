package com.cmc.dao;

import com.cmc.model.MoengageImportLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MoengageImportLogDAO extends MongoRepository<MoengageImportLog, Integer> {

}
