package capstone.cycle.file.repository;

import capstone.cycle.file.entity.File;
import org.springframework.data.repository.CrudRepository;

public interface FileRepository extends CrudRepository<File, Long> {

}
