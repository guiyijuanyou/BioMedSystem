package com.cqutcm.biomed.repository;

import com.cqutcm.biomed.model.FileAsset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FileAssetRepository {
    private final JdbcTemplate jdbcTemplate;

    public FileAssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FileAsset> findAll() {
        return jdbcTemplate.query(
                "SELECT id, file_name, category, size_bytes, storage_path, created_at FROM file_asset ORDER BY created_at DESC",
                this::mapRow
        );
    }

    public Optional<FileAsset> findById(String id) {
        List<FileAsset> files = jdbcTemplate.query(
                "SELECT id, file_name, category, size_bytes, storage_path, created_at FROM file_asset WHERE id = ?",
                this::mapRow,
                id
        );
        return files.stream().findFirst();
    }

    public void insert(FileAsset file) {
        jdbcTemplate.update(
                "INSERT INTO file_asset (id, file_name, category, size_bytes, storage_path, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                file.getId(),
                file.getFileName(),
                file.getCategory(),
                file.getSize(),
                file.getPath(),
                Timestamp.valueOf(file.getCreatedAt())
        );
    }

    private FileAsset mapRow(ResultSet rs, int rowNum) throws SQLException {
        FileAsset file = new FileAsset();
        file.setId(rs.getString("id"));
        file.setFileName(rs.getString("file_name"));
        file.setCategory(rs.getString("category"));
        file.setSize(rs.getLong("size_bytes"));
        file.setPath(rs.getString("storage_path"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        file.setCreatedAt(createdAt == null ? LocalDateTime.now() : createdAt.toLocalDateTime());
        return file;
    }
}

