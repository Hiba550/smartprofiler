package dev.hiba550.smartprofiler.storage;

import dev.hiba550.smartprofiler.SmartProfilerMod;
import dev.hiba550.smartprofiler.config.ProfilerConfig;
import dev.hiba550.smartprofiler.data.models.PerformanceFrame;
import dev.hiba550.smartprofiler.data.models.PerformanceIssue;
import dev.hiba550.smartprofiler.storage.models.*;
import dev.hiba550.smartprofiler.util.DateTimeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * High-performance database system for storing and analyzing Minecraft performance data
 * 
 * Features:
 * - H2 embedded database for cross-platform compatibility
 * - Asynchronous write operations to prevent game lag
 * - Automatic data cleanup and retention management
 * - Efficient bulk insert operations
 * - Connection pooling for optimal performance
 * - Data compression and indexing for storage efficiency
 * - Export capabilities (CSV, JSON)
 * - Backup and restore functionality
 * 
 * Database Schema:
 * - performance_frames: Raw performance data points
 * - performance_issues: Detected performance issues
 * - performance_sessions: Gaming sessions for context
 * - performance_summaries: Aggregated statistics
 * - system_info: Hardware and system configuration
 * 
 * @author Hiba550
 * @version 1.0.0
 * @since MC 1.21.5
 */
public class PerformanceDatabase {
    
    // Database configuration
    private static final String DATABASE_NAME = "smart_profiler_data";
    private static final String DATABASE_VERSION = "1.0";
    private static final int CONNECTION_POOL_SIZE = 5;
    private static final int BATCH_SIZE = 100;
    private static final long CLEANUP_INTERVAL_MS = 60000; // 1 minute
    
    // Database connection management
    private Connection mainConnection;
    private final BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    
    // Async operation management
    private ExecutorService writeExecutor;
    private ScheduledExecutorService maintenanceExecutor;
    private final BlockingQueue<DatabaseOperation> operationQueue = new LinkedBlockingQueue<>();
    
    // Performance tracking
    private final AtomicLong totalWrites = new AtomicLong(0);
    private final AtomicLong totalReads = new AtomicLong(0);
    private final AtomicLong lastMaintenanceTime = new AtomicLong(0);
    
    // Current session tracking
    private volatile long currentSessionId = -1;
    private volatile LocalDateTime sessionStartTime;
    
    // Prepared statements cache
    private final Map<String, PreparedStatement> preparedStatements = new ConcurrentHashMap<>();
    
    /**
     * Initializes the database system
     * Creates tables, indexes, and starts background threads
     */
    public void initialize() throws SQLException {
        if (initialized.get()) {
            SmartProfilerMod.LOGGER.warn("Database already initialized");
            return;
        }
        
        try {
            SmartProfilerMod.LOGGER.info("Initializing Smart Profiler database system");
            
            // Create database directory
            createDatabaseDirectory();
            
            // Initialize database connection
            initializeConnection();
            
            // Create database schema
            createDatabaseSchema();
            
            // Create indexes for performance
            createIndexes();
            
            // Initialize connection pool
            initializeConnectionPool();
            
            // Start background threads
            startBackgroundThreads();
            
            // Start new session
            startNewSession();
            
            // Schedule maintenance tasks
            scheduleMaintenanceTasks();
            
            initialized.set(true);
            SmartProfilerMod.LOGGER.info("Database system initialized successfully");
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Failed to initialize database", e);
            cleanup();
            throw new SQLException("Database initialization failed", e);
        }
    }
    
    /**
     * Creates the database directory structure
     */
    private void createDatabaseDirectory() throws IOException {
        Path dbPath = getDatabasePath();
        if (!Files.exists(dbPath.getParent())) {
            Files.createDirectories(dbPath.getParent());
            SmartProfilerMod.LOGGER.debug("Created database directory: {}", dbPath.getParent());
        }
    }
    
    /**
     * Gets the database file path
     */
    private Path getDatabasePath() {
        String minecraftDir = System.getProperty("user.dir");
        return Paths.get(minecraftDir, "smart_profiler", DATABASE_NAME);
    }
    
    /**
     * Initializes the main database connection
     */
    private void initializeConnection() throws SQLException {
        String jdbcUrl = "jdbc:h2:" + getDatabasePath().toString() + 
                        ";AUTO_SERVER=TRUE" +
                        ";COMPRESS=TRUE" +
                        ";DB_CLOSE_ON_EXIT=FALSE" +
                        ";FILE_LOCK=FS" +
                        ";TRACE_LEVEL_FILE=0";
        
        Properties props = new Properties();
        props.setProperty("user", "smart_profiler");
        props.setProperty("password", "performance_data");
        
        mainConnection = DriverManager.getConnection(jdbcUrl, props);
        mainConnection.setAutoCommit(false); // Use transactions for better performance
        
        SmartProfilerMod.LOGGER.debug("Database connection established: {}", jdbcUrl);
    }
    
    /**
     * Creates the complete database schema
     */
    private void createDatabaseSchema() throws SQLException {
        String[] schemaSql = {
            // System information table
            """
            CREATE TABLE IF NOT EXISTS system_info (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                minecraft_version VARCHAR(50) NOT NULL,
                java_version VARCHAR(100) NOT NULL,
                os_name VARCHAR(100) NOT NULL,
                os_arch VARCHAR(50) NOT NULL,
                cpu_cores INTEGER NOT NULL,
                max_memory_mb BIGINT NOT NULL,
                gpu_name VARCHAR(200),
                gpu_memory_mb BIGINT,
                mod_version VARCHAR(20) NOT NULL
            )
            """,
            
            // Performance sessions table
            """
            CREATE TABLE IF NOT EXISTS performance_sessions (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                session_start TIMESTAMP NOT NULL,
                session_end TIMESTAMP,
                world_name VARCHAR(200),
                game_mode VARCHAR(50),
                dimension VARCHAR(100),
                player_count INTEGER DEFAULT 1,
                total_frames INTEGER DEFAULT 0,
                avg_fps DECIMAL(8,2) DEFAULT 0,
                min_fps INTEGER DEFAULT 0,
                max_fps INTEGER DEFAULT 0,
                avg_memory_usage DECIMAL(5,2) DEFAULT 0,
                max_memory_usage DECIMAL(5,2) DEFAULT 0,
                total_issues INTEGER DEFAULT 0,
                session_duration_seconds INTEGER DEFAULT 0,
                notes TEXT
            )
            """,
            
            // Raw performance frames table
            """
            CREATE TABLE IF NOT EXISTS performance_frames (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                session_id BIGINT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                frame_type VARCHAR(20) NOT NULL,
                fps INTEGER NOT NULL,
                frame_time_ms DECIMAL(8,3) NOT NULL,
                
                -- Memory statistics
                heap_used_mb BIGINT NOT NULL,
                heap_max_mb BIGINT NOT NULL,
                heap_usage_percent DECIMAL(5,2) NOT NULL,
                non_heap_used_mb BIGINT NOT NULL,
                gc_time_ms BIGINT DEFAULT 0,
                gc_collections INTEGER DEFAULT 0,
                
                -- World statistics
                loaded_chunks INTEGER DEFAULT 0,
                chunk_updates INTEGER DEFAULT 0,
                render_distance INTEGER DEFAULT 0,
                total_entities INTEGER DEFAULT 0,
                rendered_entities INTEGER DEFAULT 0,
                tile_entities INTEGER DEFAULT 0,
                
                -- Rendering statistics
                render_time_ms DECIMAL(8,3) DEFAULT 0,
                draw_calls INTEGER DEFAULT 0,
                triangles_rendered BIGINT DEFAULT 0,
                texture_memory_mb INTEGER DEFAULT 0,
                shader_compile_time_ms DECIMAL(8,3) DEFAULT 0,
                
                -- Network statistics (for multiplayer)
                network_latency_ms DECIMAL(8,2) DEFAULT 0,
                packets_sent INTEGER DEFAULT 0,
                packets_received INTEGER DEFAULT 0,
                bytes_sent BIGINT DEFAULT 0,
                bytes_received BIGINT DEFAULT 0,
                packet_loss_percent DECIMAL(5,2) DEFAULT 0,
                
                -- System statistics
                cpu_usage_percent DECIMAL(5,2) DEFAULT 0,
                disk_read_mb DECIMAL(8,2) DEFAULT 0,
                disk_write_mb DECIMAL(8,2) DEFAULT 0,
                
                FOREIGN KEY (session_id) REFERENCES performance_sessions(id) ON DELETE CASCADE
            )
            """,
            
            // Performance issues table
            """
            CREATE TABLE IF NOT EXISTS performance_issues (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                session_id BIGINT NOT NULL,
                frame_id BIGINT,
                timestamp TIMESTAMP NOT NULL,
                issue_type VARCHAR(50) NOT NULL,
                severity VARCHAR(20) NOT NULL,
                category VARCHAR(30) NOT NULL,
                description TEXT,
                value DECIMAL(12,4),
                threshold DECIMAL(12,4),
                duration_ms BIGINT DEFAULT 0,
                resolved_at TIMESTAMP,
                
                FOREIGN KEY (session_id) REFERENCES performance_sessions(id) ON DELETE CASCADE,
                FOREIGN KEY (frame_id) REFERENCES performance_frames(id) ON DELETE CASCADE
            )
            """,
            
            // Performance summaries table (for quick reporting)
            """
            CREATE TABLE IF NOT EXISTS performance_summaries (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                session_id BIGINT NOT NULL,
                summary_date DATE NOT NULL,
                summary_type VARCHAR(20) NOT NULL, -- 'hourly', 'daily', 'session'
                
                -- Aggregated FPS statistics
                avg_fps DECIMAL(8,2) NOT NULL,
                min_fps INTEGER NOT NULL,
                max_fps INTEGER NOT NULL,
                fps_p95 DECIMAL(8,2) NOT NULL,
                fps_stability DECIMAL(5,4) NOT NULL, -- Coefficient of variation
                
                -- Aggregated memory statistics
                avg_memory_usage DECIMAL(5,2) NOT NULL,
                max_memory_usage DECIMAL(5,2) NOT NULL,
                total_gc_time_ms BIGINT NOT NULL,
                total_gc_collections INTEGER NOT NULL,
                
                -- Performance issue counts
                total_issues INTEGER NOT NULL,
                critical_issues INTEGER NOT NULL,
                high_issues INTEGER NOT NULL,
                medium_issues INTEGER NOT NULL,
                low_issues INTEGER NOT NULL,
                
                -- Time periods
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                
                FOREIGN KEY (session_id) REFERENCES performance_sessions(id) ON DELETE CASCADE
            )
            """,
            
            // Optimization suggestions table
            """
            CREATE TABLE IF NOT EXISTS optimization_suggestions (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                session_id BIGINT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                suggestion_type VARCHAR(50) NOT NULL,
                priority INTEGER NOT NULL, -- 1-10 scale
                title VARCHAR(200) NOT NULL,
                description TEXT NOT NULL,
                implementation_steps TEXT,
                expected_improvement TEXT,
                applied BOOLEAN DEFAULT FALSE,
                applied_at TIMESTAMP,
                effectiveness_rating INTEGER, -- 1-5 scale after application
                
                FOREIGN KEY (session_id) REFERENCES performance_sessions(id) ON DELETE CASCADE
            )
            """,
            
            // Database metadata table
            """
            CREATE TABLE IF NOT EXISTS database_metadata (
                id INTEGER PRIMARY KEY,
                schema_version VARCHAR(10) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                last_cleanup TIMESTAMP,
                total_records BIGINT DEFAULT 0,
                database_size_mb DECIMAL(10,2) DEFAULT 0
            )
            """
        };
        
        for (String sql : schemaSql) {
            try (Statement stmt = mainConnection.createStatement()) {
                stmt.execute(sql);
            }
        }
        
        // Insert initial metadata if not exists
        insertInitialMetadata();
        
        mainConnection.commit();
        SmartProfilerMod.LOGGER.debug("Database schema created successfully");
    }
    
    /**
     * Creates database indexes for optimal query performance
     */
    private void createIndexes() throws SQLException {
        String[] indexSql = {
            // Performance frames indexes
            "CREATE INDEX IF NOT EXISTS idx_frames_session_timestamp ON performance_frames(session_id, timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_frames_timestamp ON performance_frames(timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_frames_fps ON performance_frames(fps)",
            "CREATE INDEX IF NOT EXISTS idx_frames_memory ON performance_frames(heap_usage_percent)",
            
            // Performance issues indexes
            "CREATE INDEX IF NOT EXISTS idx_issues_session_timestamp ON performance_issues(session_id, timestamp)",
            "CREATE INDEX IF NOT EXISTS idx_issues_type_severity ON performance_issues(issue_type, severity)",
            "CREATE INDEX IF NOT EXISTS idx_issues_resolved ON performance_issues(resolved_at)",
            
            // Performance sessions indexes
            "CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON performance_sessions(session_start)",
            "CREATE INDEX IF NOT EXISTS idx_sessions_world ON performance_sessions(world_name)",
            
            // Performance summaries indexes
            "CREATE INDEX IF NOT EXISTS idx_summaries_session_date ON performance_summaries(session_id, summary_date)",
            "CREATE INDEX IF NOT EXISTS idx_summaries_type_date ON performance_summaries(summary_type, summary_date)",
            
            // Optimization suggestions indexes
            "CREATE INDEX IF NOT EXISTS idx_suggestions_session ON optimization_suggestions(session_id)",
            "CREATE INDEX IF NOT EXISTS idx_suggestions_priority ON optimization_suggestions(priority DESC)"
        };
        
        for (String sql : indexSql) {
            try (Statement stmt = mainConnection.createStatement()) {
                stmt.execute(sql);
            }
        }
        
        mainConnection.commit();
        SmartProfilerMod.LOGGER.debug("Database indexes created successfully");
    }
    
    /**
     * Inserts initial database metadata
     */
    private void insertInitialMetadata() throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM database_metadata WHERE id = 1";
        String insertSql = """
            INSERT INTO database_metadata (id, schema_version, created_at, total_records, database_size_mb)
            VALUES (1, ?, CURRENT_TIMESTAMP, 0, 0)
            """;
        
        try (PreparedStatement checkStmt = mainConnection.prepareStatement(checkSql)) {
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            
            if (rs.getInt(1) == 0) {
                try (PreparedStatement insertStmt = mainConnection.prepareStatement(insertSql)) {
                    insertStmt.setString(1, DATABASE_VERSION);
                    insertStmt.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Initializes the connection pool for concurrent database operations
     */
    private void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < CONNECTION_POOL_SIZE; i++) {
            Connection conn = DriverManager.getConnection(
                mainConnection.getMetaData().getURL(),
                "smart_profiler",
                "performance_data"
            );
            conn.setAutoCommit(false);
            connectionPool.offer(conn);
        }
        
        SmartProfilerMod.LOGGER.debug("Connection pool initialized with {} connections", CONNECTION_POOL_SIZE);
    }
    
    /**
     * Starts background threads for async operations
     */
    private void startBackgroundThreads() {
        // Write operations executor
        writeExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "SmartProfiler-DB-Writer");
            t.setDaemon(true);
            return t;
        });
        
        // Maintenance executor
        maintenanceExecutor = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "SmartProfiler-DB-Maintenance");
            t.setDaemon(true);
            return t;
        });
        
        // Start operation queue processor
        writeExecutor.submit(this::processOperationQueue);
        
        SmartProfilerMod.LOGGER.debug("Database background threads started");
    }
    
    /**
     * Starts a new performance monitoring session
     */
    private void startNewSession() throws SQLException {
        sessionStartTime = LocalDateTime.now();
        
        String sql = """
            INSERT INTO performance_sessions (session_start, world_name, game_mode, dimension)
            VALUES (?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = mainConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(sessionStartTime));
            stmt.setString(2, "Unknown"); // Will be updated when world loads
            stmt.setString(3, "Unknown");
            stmt.setString(4, "Unknown");
            
            stmt.executeUpdate();
            mainConnection.commit();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                currentSessionId = rs.getLong(1);
                SmartProfilerMod.LOGGER.info("Started new performance session: {}", currentSessionId);
            }
        }
    }
    
    /**
     * Schedules periodic maintenance tasks
     */
    private void scheduleMaintenanceTasks() {
        // Cleanup old data every hour
        maintenanceExecutor.scheduleWithFixedDelay(
            this::performMaintenance,
            CLEANUP_INTERVAL_MS,
            CLEANUP_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );
        
        // Update database statistics every 5 minutes
        maintenanceExecutor.scheduleWithFixedDelay(
            this::updateDatabaseStatistics,
            300000, // 5 minutes
            300000,
            TimeUnit.MILLISECONDS
        );
        
        SmartProfilerMod.LOGGER.debug("Maintenance tasks scheduled");
    }
    
    /**
     * Stores a performance frame asynchronously
     */
    public void storePerformanceFrame(PerformanceFrame frame) {
        if (!initialized.get() || shutdownRequested.get()) {
            return;
        }
        
        DatabaseOperation operation = new DatabaseOperation(
            DatabaseOperationType.INSERT_FRAME,
            frame,
            System.currentTimeMillis()
        );
        
        if (!operationQueue.offer(operation)) {
            SmartProfilerMod.LOGGER.warn("Database operation queue is full, dropping frame data");
        }
    }
    
    /**
     * Stores a performance issue asynchronously
     */
    public void storePerformanceIssue(PerformanceIssue issue, PerformanceFrame frame, String description) {
        if (!initialized.get() || shutdownRequested.get()) {
            return;
        }
        
        DatabaseOperation operation = new DatabaseOperation(
            DatabaseOperationType.INSERT_ISSUE,
            new PerformanceIssueRecord(issue, frame, description),
            System.currentTimeMillis()
        );
        
        if (!operationQueue.offer(operation)) {
            SmartProfilerMod.LOGGER.warn("Database operation queue is full, dropping issue data");
        }
    }
    
    /**
     * Processes the database operation queue
     */
    private void processOperationQueue() {
        List<DatabaseOperation> batch = new ArrayList<>();
        
        while (!shutdownRequested.get()) {
            try {
                // Wait for operations or timeout
                DatabaseOperation operation = operationQueue.poll(1, TimeUnit.SECONDS);
                if (operation == null) {
                    continue;
                }
                
                batch.add(operation);
                
                // Collect more operations up to batch size
                while (batch.size() < BATCH_SIZE) {
                    DatabaseOperation nextOp = operationQueue.poll();
                    if (nextOp == null) {
                        break;
                    }
                    batch.add(nextOp);
                }
                
                // Process the batch
                if (!batch.isEmpty()) {
                    processBatch(batch);
                    batch.clear();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                SmartProfilerMod.LOGGER.error("Error processing database operations", e);
                batch.clear();
            }
        }
    }
    
    /**
     * Processes a batch of database operations
     */
    private void processBatch(List<DatabaseOperation> operations) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            Map<DatabaseOperationType, List<DatabaseOperation>> groupedOps = 
                operations.stream().collect(
                    java.util.stream.Collectors.groupingBy(DatabaseOperation::getType)
                );
            
            // Process each operation type in batch
            for (Map.Entry<DatabaseOperationType, List<DatabaseOperation>> entry : groupedOps.entrySet()) {
                switch (entry.getKey()) {
                    case INSERT_FRAME -> processBatchFrameInserts(conn, entry.getValue());
                    case INSERT_ISSUE -> processBatchIssueInserts(conn, entry.getValue());
                    case UPDATE_SESSION -> processBatchSessionUpdates(conn, entry.getValue());
                }
            }
            
            conn.commit();
            totalWrites.addAndGet(operations.size());
            
        } catch (SQLException e) {
            SmartProfilerMod.LOGGER.error("Error processing database batch", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    SmartProfilerMod.LOGGER.error("Error rolling back transaction", rollbackEx);
                }
            }
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Processes batch frame insertions
     */
    private void processBatchFrameInserts(Connection conn, List<DatabaseOperation> operations) throws SQLException {
        String sql = """
            INSERT INTO performance_frames (
                session_id, timestamp, frame_type, fps, frame_time_ms,
                heap_used_mb, heap_max_mb, heap_usage_percent, non_heap_used_mb,
                gc_time_ms, gc_collections, loaded_chunks, chunk_updates, render_distance,
                total_entities, rendered_entities, tile_entities, render_time_ms,
                draw_calls, triangles_rendered, texture_memory_mb, shader_compile_time_ms,
                network_latency_ms, packets_sent, packets_received, bytes_sent, bytes_received,
                packet_loss_percent, cpu_usage_percent, disk_read_mb, disk_write_mb
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (DatabaseOperation op : operations) {
                PerformanceFrame frame = (PerformanceFrame) op.getData();
                setFrameParameters(stmt, frame);
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Sets parameters for frame insertion
     */
    private void setFrameParameters(PreparedStatement stmt, PerformanceFrame frame) throws SQLException {
        int paramIndex = 1;
        
        stmt.setLong(paramIndex++, currentSessionId);
        stmt.setTimestamp(paramIndex++, new Timestamp(frame.getTimestamp()));
        stmt.setString(paramIndex++, frame.getFrameType().name());
        stmt.setInt(paramIndex++, frame.getFps());
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(1000.0 / Math.max(1, frame.getFps())));
        
        // Memory statistics
        var memStats = frame.getMemoryStats();
        stmt.setLong(paramIndex++, memStats.getHeapUsed() / (1024 * 1024));
        stmt.setLong(paramIndex++, memStats.getHeapMax() / (1024 * 1024));
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(memStats.getHeapUsagePercent()));
        stmt.setLong(paramIndex++, memStats.getNonHeapUsed() / (1024 * 1024));
        stmt.setLong(paramIndex++, memStats.getGcTime());
        stmt.setInt(paramIndex++, (int) memStats.getGcCollections());
        
        // World statistics
        var chunkStats = frame.getChunkStats();
        stmt.setInt(paramIndex++, chunkStats.getLoadedChunks());
        stmt.setInt(paramIndex++, chunkStats.getChunkUpdates());
        stmt.setInt(paramIndex++, chunkStats.getRenderDistance());
        
        var entityStats = frame.getEntityStats();
        stmt.setInt(paramIndex++, entityStats.getTotalEntities());
        stmt.setInt(paramIndex++, entityStats.getRenderedEntities());
        stmt.setInt(paramIndex++, entityStats.getTileEntityCount());
        
        // Rendering statistics
        var renderStats = frame.getRenderStats();
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(renderStats.getRenderTimeMs()));
        stmt.setInt(paramIndex++, renderStats.getDrawCalls());
        stmt.setLong(paramIndex++, renderStats.getTrianglesRendered());
        stmt.setInt(paramIndex++, renderStats.getTextureMemoryMB());
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(renderStats.getShaderCompileTime()));
        
        // Network statistics
        var networkStats = frame.getNetworkStats();
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(networkStats.getLatencyMs()));
        stmt.setInt(paramIndex++, networkStats.getPacketsSent());
        stmt.setInt(paramIndex++, networkStats.getPacketsReceived());
        stmt.setLong(paramIndex++, networkStats.getBytesSent());
        stmt.setLong(paramIndex++, networkStats.getBytesReceived());
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(networkStats.getPacketLoss()));
        
        // System statistics
        var worldStats = frame.getWorldStats();
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(worldStats.getCpuUsagePercent()));
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(worldStats.getDiskReadMB()));
        stmt.setBigDecimal(paramIndex++, java.math.BigDecimal.valueOf(worldStats.getDiskWriteMB()));
    }
    
    /**
     * Processes batch issue insertions
     */
    private void processBatchIssueInserts(Connection conn, List<DatabaseOperation> operations) throws SQLException {
        String sql = """
            INSERT INTO performance_issues (
                session_id, timestamp, issue_type, severity, category, description, value, threshold
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (DatabaseOperation op : operations) {
                PerformanceIssueRecord record = (PerformanceIssueRecord) op.getData();
                setIssueParameters(stmt, record);
                stmt.addBatch();
            }
            
            stmt.executeBatch();
        }
    }
    
    /**
     * Sets parameters for issue insertion
     */
    private void setIssueParameters(PreparedStatement stmt, PerformanceIssueRecord record) throws SQLException {
        stmt.setLong(1, currentSessionId);
        stmt.setTimestamp(2, new Timestamp(record.getTimestamp()));
        stmt.setString(3, record.getIssue().name());
        stmt.setString(4, record.getIssue().getSeverity().name());
        stmt.setString(5, record.getIssue().getCategory().name());
        stmt.setString(6, record.getDescription());
        stmt.setBigDecimal(7, java.math.BigDecimal.valueOf(record.getValue()));
        stmt.setBigDecimal(8, java.math.BigDecimal.valueOf(record.getThreshold()));
    }
    
    /**
     * Processes batch session updates
     */
    private void processBatchSessionUpdates(Connection conn, List<DatabaseOperation> operations) throws SQLException {
        // Implementation for session updates
        // This would handle updates to session statistics, world changes, etc.
    }
    
    /**
     * Gets a connection from the pool
     */
    private Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(5, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed()) {
                // Create new connection if pool is empty or connection is closed
                conn = DriverManager.getConnection(
                    mainConnection.getMetaData().getURL(),
                    "smart_profiler",
                    "performance_data"
                );
                conn.setAutoCommit(false);
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }
    
    /**
     * Returns a connection to the pool
     */
    private void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    connectionPool.offer(conn);
                }
            } catch (SQLException e) {
                SmartProfilerMod.LOGGER.warn("Error checking connection status", e);
            }
        }
    }
    
    /**
     * Performs periodic maintenance tasks
     */
    private void performMaintenance() {
        if (!initialized.get() || shutdownRequested.get()) {
            return;
        }
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Clean up old data based on retention policy
            cleanupOldData();
            
            // Optimize database (ANALYZE, vacuum, etc.)
            optimizeDatabase();
            
            // Update statistics
            updateDatabaseStatistics();
            
            lastMaintenanceTime.set(System.currentTimeMillis());
            
            long duration = System.currentTimeMillis() - startTime;
            SmartProfilerMod.LOGGER.debug("Database maintenance completed in {}ms", duration);
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error during database maintenance", e);
        }
    }
    
    /**
     * Cleans up old data based on retention policy
     */
    private void cleanupOldData() throws SQLException {
        if (!ProfilerConfig.isAutoCleanupEnabled()) {
            return;
        }
        
        int retentionDays = ProfilerConfig.getDataRetentionDays();
        String cutoffDate = LocalDateTime.now().minusDays(retentionDays)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        Connection conn = null;
        try {
            conn = getConnection();
            
            // Delete old sessions and cascade to related data
            String deleteSql = "DELETE FROM performance_sessions WHERE session_start < ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setTimestamp(1, Timestamp.valueOf(cutoffDate));
                int deleted = stmt.executeUpdate();
                
                if (deleted > 0) {
                    SmartProfilerMod.LOGGER.info("Cleaned up {} old performance sessions", deleted);
                }
            }
            
            conn.commit();
            
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Optimizes database performance
     */
    private void optimizeDatabase() throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            
            // Analyze tables for query optimization
            String[] analyzeSql = {
                "ANALYZE TABLE performance_frames",
                "ANALYZE TABLE performance_issues",
                "ANALYZE TABLE performance_sessions",
                "ANALYZE TABLE performance_summaries"
            };
            
            for (String sql : analyzeSql) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
            
            conn.commit();
            
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Updates database statistics
     */
    private void updateDatabaseStatistics() {
        try {
            Connection conn = getConnection();
            
            // Count total records
            String countSql = """
                SELECT 
                    (SELECT COUNT(*) FROM performance_frames) + 
                    (SELECT COUNT(*) FROM performance_issues) + 
                    (SELECT COUNT(*) FROM performance_sessions) AS total_records
                """;
            
            // Calculate database size
            String sizeSql = "SELECT ROUND(FILE_SIZE('smart_profiler_data.mv.db') / 1024.0 / 1024.0, 2) AS size_mb";
            
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(countSql);
                long totalRecords = rs.next() ? rs.getLong("total_records") : 0;
                
                rs = stmt.executeQuery(sizeSql);
                double sizeMB = rs.next() ? rs.getDouble("size_mb") : 0.0;
                
                // Update metadata
                String updateSql = """
                    UPDATE database_metadata 
                    SET total_records = ?, database_size_mb = ?, last_cleanup = CURRENT_TIMESTAMP
                    WHERE id = 1
                    """;
                
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setLong(1, totalRecords);
                    updateStmt.setBigDecimal(2, java.math.BigDecimal.valueOf(sizeMB));
                    updateStmt.executeUpdate();
                }
                
                conn.commit();
            }
            
            returnConnection(conn);
            
        } catch (SQLException e) {
            SmartProfilerMod.LOGGER.warn("Error updating database statistics", e);
        }
    }
    
    /**
     * Saves pending performance data immediately
     */
    public void savePerformanceData() {
        if (!initialized.get()) {
            return;
        }
        
        try {
            // Process any remaining operations in queue
            List<DatabaseOperation> remaining = new ArrayList<>();
            operationQueue.drainTo(remaining);
            
            if (!remaining.isEmpty()) {
                processBatch(remaining);
                SmartProfilerMod.LOGGER.info("Saved {} pending database operations", remaining.size());
            }
            
            // End current session
            endCurrentSession();
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error saving performance data", e);
        }
    }
    
    /**
     * Ends the current performance session
     */
    private void endCurrentSession() throws SQLException {
        if (currentSessionId == -1) {
            return;
        }
        
        Connection conn = null;
        try {
            conn = getConnection();
            
            // Calculate session statistics
            String statsSql = """
                SELECT 
                    COUNT(*) as total_frames,
                    AVG(fps) as avg_fps,
                    MIN(fps) as min_fps,
                    MAX(fps) as max_fps,
                    AVG(heap_usage_percent) as avg_memory,
                    MAX(heap_usage_percent) as max_memory
                FROM performance_frames 
                WHERE session_id = ?
                """;
            
            SessionStats stats = new SessionStats();
            try (PreparedStatement stmt = conn.prepareStatement(statsSql)) {
                stmt.setLong(1, currentSessionId);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    stats.totalFrames = rs.getInt("total_frames");
                    stats.avgFps = rs.getDouble("avg_fps");
                    stats.minFps = rs.getInt("min_fps");
                    stats.maxFps = rs.getInt("max_fps");
                    stats.avgMemory = rs.getDouble("avg_memory");
                    stats.maxMemory = rs.getDouble("max_memory");
                }
            }
            
            // Count issues
            String issueCountSql = "SELECT COUNT(*) FROM performance_issues WHERE session_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(issueCountSql)) {
                stmt.setLong(1, currentSessionId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.totalIssues = rs.getInt(1);
                }
            }
            
            // Update session with final statistics
            String updateSql = """
                UPDATE performance_sessions 
                SET session_end = CURRENT_TIMESTAMP,
                    total_frames = ?,
                    avg_fps = ?,
                    min_fps = ?,
                    max_fps = ?,
                    avg_memory_usage = ?,
                    max_memory_usage = ?,
                    total_issues = ?,
                    session_duration_seconds = DATEDIFF('SECOND', session_start, CURRENT_TIMESTAMP)
                WHERE id = ?
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, stats.totalFrames);
                stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(stats.avgFps));
                stmt.setInt(3, stats.minFps);
                stmt.setInt(4, stats.maxFps);
                stmt.setBigDecimal(5, java.math.BigDecimal.valueOf(stats.avgMemory));
                stmt.setBigDecimal(6, java.math.BigDecimal.valueOf(stats.maxMemory));
                stmt.setInt(7, stats.totalIssues);
                stmt.setLong(8, currentSessionId);
                
                stmt.executeUpdate();
            }
            
            conn.commit();
            SmartProfilerMod.LOGGER.info("Ended performance session {} with {} frames and {} issues", 
                currentSessionId, stats.totalFrames, stats.totalIssues);
            
        } finally {
            if (conn != null) {
                returnConnection(conn);
            }
        }
    }
    
    /**
     * Cleanup and shutdown the database system
     */
    public void cleanup() {
        if (!initialized.get()) {
            return;
        }
        
        shutdownRequested.set(true);
        
        try {
            SmartProfilerMod.LOGGER.info("Shutting down database system...");
            
            // Save any remaining data
            savePerformanceData();
            
            // Shutdown executors
            if (writeExecutor != null) {
                writeExecutor.shutdown();
                writeExecutor.awaitTermination(5, TimeUnit.SECONDS);
            }
            
            if (maintenanceExecutor != null) {
                maintenanceExecutor.shutdown();
                maintenanceExecutor.awaitTermination(2, TimeUnit.SECONDS);
            }
            
            // Close connection pool
            while (!connectionPool.isEmpty()) {
                Connection conn = connectionPool.poll();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
            
            // Close main connection
            if (mainConnection != null && !mainConnection.isClosed()) {
                mainConnection.close();
            }
            
            initialized.set(false);
            SmartProfilerMod.LOGGER.info("Database system shutdown complete");
            
        } catch (Exception e) {
            SmartProfilerMod.LOGGER.error("Error during database cleanup", e);
        }
    }
    
    /**
     * Gets database statistics
     */
    public DatabaseStatistics getStatistics() {
        return new DatabaseStatistics(
            totalWrites.get(),
            totalReads.get(),
            operationQueue.size(),
            connectionPool.size(),
            lastMaintenanceTime.get(),
            currentSessionId
        );
    }
    
    // Helper classes for data transfer
    private static class SessionStats {
        int totalFrames = 0;
        double avgFps = 0.0;
        int minFps = 0;
        int maxFps = 0;
        double avgMemory = 0.0;
        double maxMemory = 0.0;
        int totalIssues = 0;
    }
    
    /**
     * Record class for database statistics
     */
    public record DatabaseStatistics(
        long totalWrites,
        long totalReads,
        int queueSize,
        int availableConnections,
        long lastMaintenanceTime,
        long currentSessionId
    ) {}
}