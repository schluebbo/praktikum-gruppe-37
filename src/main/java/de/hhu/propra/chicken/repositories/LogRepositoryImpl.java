package de.hhu.propra.chicken.repositories;

import de.hhu.propra.chicken.domain.model.Log;
import de.hhu.propra.chicken.domain.service.LogRepository;
import de.hhu.propra.chicken.repositories.dataRepo.DBLogRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Not important here")
public class LogRepositoryImpl implements LogRepository {

    DBLogRepository logs;

    public LogRepositoryImpl(DBLogRepository logs) {
        this.logs = logs;
    }

    @Override
    public void save(Log log) {
        logs.save(log);
    }

    @Override
    public List<Log> findAll() {
        return logs.findAll();
    }

    @Override
    public void removeAll() {

    }
}
