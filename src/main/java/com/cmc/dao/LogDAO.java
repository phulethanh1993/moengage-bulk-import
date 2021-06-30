package com.cmc.dao;

import com.cmc.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDAO extends MongoRepository<Log, String> {

}
