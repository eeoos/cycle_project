package capstone.cycle.file.service;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.FileGroupDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.file.error.FileErrorResult;
import capstone.cycle.file.error.FileException;
import capstone.cycle.file.repository.FileGroupRepository;
import capstone.cycle.file.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final FileGroupRepository fileGroupRepository;

    @Value("${file.upload-dir}")
    private String UPLOAD_DIR;

    @Override
    @Transactional
    public FileDTO uploadFile(MultipartFile file, String contentName) {
        try {
            String fileName = generateFileName(file);
            Path targetLocation = getTargetLocation(contentName, fileName);

            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            File fileEntity = createFileEntity(file, targetLocation);  // null 파라미터 제거
            File savedFile = fileRepository.save(fileEntity);

            return savedFile.toDTO();
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new FileException(FileErrorResult.UPLOAD_FAIL);
        }
    }

    @Override
    @Transactional
    public FileGroupDTO uploadFiles(List<MultipartFile> files, String contentName) {
        FileGroup fileGroup = FileGroup.createEmpty();
        fileGroup = fileGroupRepository.save(fileGroup);
        final Long fileGroupId = fileGroup.getId();

        List<File> uploadedFiles = files.stream()
                .map(file -> {
                    try {
                        String fileName = generateFileName(file);
                        Path targetLocation = getTargetLocation(contentName, fileName);

                        Files.createDirectories(targetLocation.getParent());
                        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                        File fileEntity = createFileEntity(file, targetLocation);  // fileGroup 파라미터 제거
                        return fileRepository.save(fileEntity);
                    } catch (IOException e) {
                        log.error("File upload failed", e);
                        throw new FileException(FileErrorResult.UPLOAD_FAIL);
                    }
                })
                .collect(Collectors.toList());

        return fileGroupRepository.findById(fileGroupId)
                .map(savedFileGroup -> {
                    FileGroup updatedFileGroup = savedFileGroup.withFiles(uploadedFiles);
                    return fileGroupRepository.save(updatedFileGroup).toDTO();
                })
                .orElseThrow(() -> new FileException(FileErrorResult.FILE_GROUP_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        File file = getFile(fileId);
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));

            // 파일이 그룹에 속해 있는 경우, 그룹에서도 제거
            if (file.getFileGroup() != null) {
                FileGroup updatedGroup = file.getFileGroup().removeFile(file);
                fileGroupRepository.save(updatedGroup);
            }

            fileRepository.delete(file);
            log.info("Successfully deleted file with ID: {}", fileId);
        } catch (IOException e) {
            log.error("File deletion failed for file ID: {}", fileId, e);
            throw new FileException(FileErrorResult.DELETE_FAIL);
        }
    }

    @Override
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FileErrorResult.FILE_NOT_FOUND));
    }

    @Override
    public FileGroup getFileGroup(Long fileGroupId) {
        return fileGroupRepository.findById(fileGroupId)
                .orElseThrow(() -> new FileException(FileErrorResult.FILE_GROUP_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteFileGroup(Long fileGroupId) {
        FileGroup fileGroup = getFileGroup(fileGroupId);
        fileGroup.getFiles().forEach(file -> {
            try {
                Files.deleteIfExists(Paths.get(file.getPath()));
            } catch (IOException e) {
                log.error("File deletion failed", e);
            }
        });
        fileGroupRepository.delete(fileGroup);
    }

    private String generateFileName(MultipartFile file) {
        return UUID.randomUUID().toString() + getFileExtension(file.getOriginalFilename());
    }

    private Path getTargetLocation(String contentName, String fileName) {
        return Paths.get(UPLOAD_DIR).resolve(contentName).resolve(fileName);
    }

    private File createFileEntity(MultipartFile file, Path targetLocation) throws IOException {
        return File.createFile(
                targetLocation.getFileName().toString(),
                file.getOriginalFilename(),
                targetLocation.toString(),
                file.getContentType(),
                file.getSize(),
                getFileExtension(file.getOriginalFilename()),
                calculateChecksum(file.getInputStream())
        );
    }


    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String calculateChecksum(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return calculateChecksum(is);
        }
    }

    private String calculateChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
