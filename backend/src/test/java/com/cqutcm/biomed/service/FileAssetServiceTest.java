package com.cqutcm.biomed.service;

import com.cqutcm.biomed.config.AppDataPathResolver;
import com.cqutcm.biomed.entity.FileAsset;
import com.cqutcm.biomed.mapper.FileAssetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileAssetServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storesValidPngInsideConfiguredDirectory() {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        FileAssetService service = new FileAssetService(mapper, tempDir.toString(), 1024, new AppDataPathResolver());
        byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00
        };
        MockMultipartFile upload = new MockMultipartFile("file", "sample.png", "image/png", png);

        FileAsset saved = service.upload(upload, "图片资料");

        assertEquals("sample.png", saved.getFileName());
        assertTrue(Path.of(saved.getStoragePath()).toAbsolutePath().normalize().startsWith(tempDir.toAbsolutePath()));
        assertTrue(Files.exists(Path.of(saved.getStoragePath())));
        verify(mapper).insert(saved);
    }

    @Test
    void acceptsPdfAndWordEvidenceFiles() {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        FileAssetService service = new FileAssetService(mapper, tempDir.toString(), 4096, new AppDataPathResolver());

        FileAsset pdf = service.upload(new MockMultipartFile(
                "file", "验收报告.pdf", "application/pdf", "%PDF-1.7 evidence".getBytes()
        ), "评价佐证");
        FileAsset word = service.upload(new MockMultipartFile(
                "file", "成果证明.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[]{0x50, 0x4B, 0x03, 0x04, 0x00}
        ), "评价佐证");

        assertEquals("验收报告.pdf", pdf.getFileName());
        assertEquals("成果证明.docx", word.getFileName());
        assertTrue(Files.exists(Path.of(pdf.getStoragePath())));
        assertTrue(Files.exists(Path.of(word.getStoragePath())));
    }

    @Test
    void reusesExistingFileWithSameContentAndCategory() throws Exception {
        FileAssetMapper mapper = mock(FileAssetMapper.class);
        FileAssetService service = new FileAssetService(mapper, tempDir.toString(), 4096, new AppDataPathResolver());
        byte[] content = "same csv content".getBytes();
        Path stored = tempDir.resolve("existing-data.csv");
        Files.write(stored, content);
        FileAsset existing = new FileAsset();
        existing.setId("existing-id");
        existing.setFileName("data.csv");
        existing.setCategory("评价佐证");
        existing.setSizeBytes(content.length);
        existing.setStoragePath(stored.toString());
        when(mapper.findByNameSizeAndCategory("data.csv", content.length, "评价佐证")).thenReturn(existing);

        FileAsset result = service.upload(
                new MockMultipartFile("file", "data.csv", "text/csv", content), "评价佐证");

        assertEquals("existing-id", result.getId());
        verify(mapper, never()).insert(org.mockito.ArgumentMatchers.any(FileAsset.class));
    }

    @Test
    void rejectsExecutableWebContent() {
        FileAssetService service = new FileAssetService(mock(FileAssetMapper.class), tempDir.toString(), 1024, new AppDataPathResolver());
        MockMultipartFile upload = new MockMultipartFile(
                "file", "attack.html", "text/html", "<script>alert(1)</script>".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.upload(upload, "教学资料"));
    }

    @Test
    void rejectsFileWhoseContentDoesNotMatchExtension() {
        FileAssetService service = new FileAssetService(mock(FileAssetMapper.class), tempDir.toString(), 1024, new AppDataPathResolver());
        MockMultipartFile upload = new MockMultipartFile(
                "file", "fake.png", "image/png", "not a png".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> service.upload(upload, "图片资料"));
    }

    @Test
    void rejectsOversizedBase64Upload() {
        FileAssetService service = new FileAssetService(mock(FileAssetMapper.class), tempDir.toString(), 4, new AppDataPathResolver());
        String content = Base64.getEncoder().encodeToString("12345".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.uploadBase64(Map.of(
                "fileName", "data.csv",
                "category", "图谱文件",
                "contentBase64", content
        )));
    }

    @Test
    void rejectsStoredPathOutsideUploadDirectory() {
        FileAssetService service = new FileAssetService(mock(FileAssetMapper.class), tempDir.toString(), 1024, new AppDataPathResolver());
        FileAsset file = new FileAsset();
        file.setStoragePath(tempDir.resolve("..").resolve("outside.txt").normalize().toString());

        assertThrows(IllegalArgumentException.class, () -> service.requireStoredFile(file));
    }
}
