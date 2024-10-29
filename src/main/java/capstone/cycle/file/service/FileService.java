package capstone.cycle.file.service;

import capstone.cycle.file.dto.FileDTO;
import capstone.cycle.file.dto.FileGroupDTO;
import capstone.cycle.file.entity.File;
import capstone.cycle.file.entity.FileGroup;
import capstone.cycle.user.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    FileDTO uploadFile(MultipartFile file, String contentName);
    FileGroupDTO uploadFiles(List<MultipartFile> files, String contentName);
    void deleteFile(Long fileId);
    File getFile(Long fileId);
    FileGroup getFileGroup(Long fileGroupId);
    void deleteFileGroup(Long fileGroupId);
}
