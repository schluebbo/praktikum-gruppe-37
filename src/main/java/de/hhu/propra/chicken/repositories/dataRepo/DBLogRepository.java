package de.hhu.propra.chicken.repositories.dataRepo;

import de.hhu.propra.chicken.domain.model.Log;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DBLogRepository extends CrudRepository<Log, Long> {

    @Override
    List<Log> findAll();
}
