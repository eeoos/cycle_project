package capstone.cycle.file.entity;

import capstone.cycle.file.dto.FileGroupDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FileGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "fileGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<File> files = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 정적 팩토리 메서드
    public static FileGroup createEmpty() {
        return new FileGroup();
    }

    // 불변 리스트 반환
    public List<File> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public FileGroup withFiles(List<File> newFiles) {
        List<File> updatedFiles = newFiles.stream()
                .map(file -> file.withFileGroup(this))
                .collect(Collectors.toList());

        return FileGroup.builder()
                .id(this.id)
                .files(updatedFiles)
                .createdAt(this.createdAt)
                .build();
    }

    // 파일 추가
    public FileGroup addFile(File file) {
        List<File> newFiles = new ArrayList<>(this.files);
        newFiles.add(file.withFileGroup(this));
        return FileGroup.builder()
                .id(this.id)
                .files(newFiles)
                .createdAt(this.createdAt)
                .build();
    }

    // 파일 제거
    public FileGroup removeFile(File file) {
        List<File> newFiles = new ArrayList<>(this.files);
        newFiles.remove(file);
        return FileGroup.builder()
                .id(this.id)
                .files(newFiles)
                .createdAt(this.createdAt)
                .build();
    }

    public FileGroupDTO toDTO() {
        return FileGroupDTO.builder()
                .id(this.id)
                .files(this.files.stream()
                        .map(File::toDTO)
                        .collect(Collectors.toList()))
                .createdAt(this.createdAt)
                .build();
    }
}