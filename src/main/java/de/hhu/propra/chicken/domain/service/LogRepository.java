package de.hhu.propra.chicken.domain.service;

import java.util.List;

import de.hhu.propra.chicken.domain.model.Log;

public interface LogRepository {

        void save(Log log);

        List<Log> findAll();

        //Testing
        void removeAll();
}

