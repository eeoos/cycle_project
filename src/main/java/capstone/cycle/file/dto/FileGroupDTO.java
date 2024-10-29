package capstone.cycle.file.dto;

import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FileGroupDTO {

    private Long id;
    private List<FileDTO> files;
    private LocalDateTime createdAt;

    public FileGroupDTO(FileGroup fileGroup) {
        this.id = fileGroup.getId();
        this.createdAt = fileGroup.getCreatedAt();
        this.files = fileGroup.getFiles().stream()
                .map(FileDTO::new)
                .collect(Collectors.toList());
    }

    public static FileGroupDTO from(FileGroup fileGroup) {
        return FileGroupDTO.builder()
                .id(fileGroup.getId())
                .createdAt(fileGroup.getCreatedAt())
                .files(fileGroup.getFiles().stream()
                        .map(FileDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }



    private FileGroup createNewFileGroup() {
        return FileGroup.builder()
                .id(this.id)
                .createdAt(this.createdAt)
                .build();
    }
}