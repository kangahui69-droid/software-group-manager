package com.softwaregroup.file.service;

import com.softwaregroup.file.dao.FileStorageDAO;
import com.softwaregroup.file.model.entity.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Sec05AuthorizationTest {
    @Mock FileStorageDAO files;
    @InjectMocks FileService service;

    @Test
    void anotherUserCannotDeleteFile() {
        FileStorage file = new FileStorage();
        file.setId(7);
        file.setCreateBy(2);
        file.setStatus(1);
        when(files.findById(7)).thenReturn(file);

        assertThat(service.deleteFile(7, 1, false).getCode()).isEqualTo(403);
        verify(files, never()).softDelete(anyInt());
    }
}
