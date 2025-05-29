package dev.hiba550.smartprofiler.storage.export;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.storage.PerformanceDatabase;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Exports performance data to CSV format for external analysis
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class CsvExporter {
    
    private static final String EXPORT_DIR = "smart_profiler/exports";
    
    /**
     * Exports performance frames to CSV
     */
    public static Path exportFramesToCsv(PerformanceDatabase database, long sessionId) throws IOException, SQLException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = String.format("performance_frames_session_%d_%s.csv", sessionId, timestamp);
        Path exportPath = Paths.get(EXPORT_DIR, filename);
        
        // Create export directory if it doesn't exist
        exportPath.getParent().toFile().mkdirs();
        
        String sql = """
            SELECT 
                timestamp, fps, frame_time_ms, heap_used_mb, heap_max_mb, heap_usage_percent,
                loaded_chunks, total_entities, render_time_ms, draw_calls, network_latency_ms
            FROM performance_frames 
            WHERE session_id = ?
            ORDER BY timestamp
            """;
        
        try (FileWriter writer = new FileWriter(exportPath.toFile());
             Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Write CSV header
            writer.write("timestamp,fps,frame_time_ms,heap_used_mb,heap_max_mb,heap_usage_percent,");
            writer.write("loaded_chunks,total_entities,render_time_ms,draw_calls,network_latency_ms\n");
            
            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                writer.write(String.format("%s,%d,%.3f,%d,%d,%.2f,%d,%d,%.3f,%d,%.2f\n",
                    rs.getTimestamp("timestamp").toString(),
                    rs.getInt("fps"),
                    rs.getBigDecimal("frame_time_ms").doubleValue(),
                    rs.getLong("heap_used_mb"),
                    rs.getLong("heap_max_mb"),
                    rs.getBigDecimal("heap_usage_percent").doubleValue(),
                    rs.getInt("loaded_chunks"),
                    rs.getInt("total_entities"),
                    rs.getBigDecimal("render_time_ms").doubleValue(),
                    rs.getInt("draw_calls"),
                    rs.getBigDecimal("network_latency_ms").doubleValue()
                ));
            }
        }
        
        SmartProfilerMod.LOGGER.info("Exported performance frames to: {}", exportPath);
        return exportPath;
    }
    
    /**
     * Exports performance issues to CSV
     */
    public static Path exportIssuesToCsv(PerformanceDatabase database, long sessionId) throws IOException, SQLException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = String.format("performance_issues_session_%d_%s.csv", sessionId, timestamp);
        Path exportPath = Paths.get(EXPORT_DIR, filename);
        
        exportPath.getParent().toFile().mkdirs();
        
        String sql = """
            SELECT 
                timestamp, issue_type, severity, category, description, value, threshold, duration_ms
            FROM performance_issues 
            WHERE session_id = ?
            ORDER BY timestamp
            """;
        
        try (FileWriter writer = new FileWriter(exportPath.toFile());
             Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Write CSV header
            writer.write("timestamp,issue_type,severity,category,description,value,threshold,duration_ms\n");
            
            stmt.setLong(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                writer.write(String.format("%s,%s,%s,%s,\"%s\",%.4f,%.4f,%d\n",
                    rs.getTimestamp("timestamp").toString(),
                    rs.getString("issue_type"),
                    rs.getString("severity"),
                    rs.getString("category"),
                    rs.getString("description").replace("\"", "\"\""), // Escape quotes
                    rs.getBigDecimal("value").doubleValue(),
                    rs.getBigDecimal("threshold").doubleValue(),
                    rs.getLong("duration_ms")
                ));
            }
        }
        
        SmartProfilerMod.LOGGER.info("Exported performance issues to: {}", exportPath);
        return exportPath;
    }
}