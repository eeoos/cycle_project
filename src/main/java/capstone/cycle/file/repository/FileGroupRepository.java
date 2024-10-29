package capstone.cycle.file.repository;

import capstone.cycle.file.entity.FileGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileGroupRepository extends CrudRepository<FileGroup, Long> {
    public FileGroup findFirstByOrderByIdDesc();

    @Query("SELECT fg FROM FileGroup fg " +
            "LEFT JOIN FETCH fg.files " +
            "WHERE fg.id = :fileGroupId")
    Optional<FileGroup> findFileGroupWithFiles(@Param("fileGroupId") Long fileGroupId);

    @Query("SELECT fg FROM FileGroup fg " +
            "LEFT JOIN FETCH fg.files " +
            "WHERE fg.id IN :fileGroupIds")
    List<FileGroup> findFileGroupsWithFiles(@Param("fileGroupIds") List<Long> fileGroupIds);
}
