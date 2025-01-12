package id.kasuma.managers;

import id.kasuma.Plugin;
import org.bukkit.Location;
import java.sql.*;

public class DatabaseManager {
    private final Plugin plugin;
    private Connection connection;
    private final String dbPath;

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        this.dbPath = plugin.getDataFolder() + "/database.db";
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                // Tabel untuk spawn location
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS spawn_location (
                        id INTEGER PRIMARY KEY,
                        world TEXT NOT NULL,
                        x DOUBLE NOT NULL,
                        y DOUBLE NOT NULL,
                        z DOUBLE NOT NULL,
                        yaw FLOAT NOT NULL,
                        pitch FLOAT NOT NULL
                    )
                """);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    public void setSpawnLocation(Location location) {
        String sql = """
            INSERT OR REPLACE INTO spawn_location (id, world, x, y, z, yaw, pitch)
            VALUES (1, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, location.getWorld().getName());
            pstmt.setDouble(2, location.getX());
            pstmt.setDouble(3, location.getY());
            pstmt.setDouble(4, location.getZ());
            pstmt.setFloat(5, location.getYaw());
            pstmt.setFloat(6, location.getPitch());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save spawn location: " + e.getMessage());
        }
    }

    public Location getSpawnLocation() {
        String sql = "SELECT * FROM spawn_location WHERE id = 1";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String worldName = rs.getString("world");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float yaw = rs.getFloat("yaw");
                float pitch = rs.getFloat("pitch");

                return new Location(
                    plugin.getServer().getWorld(worldName),
                    x, y, z, yaw, pitch
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get spawn location: " + e.getMessage());
        }

        return null;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
} 