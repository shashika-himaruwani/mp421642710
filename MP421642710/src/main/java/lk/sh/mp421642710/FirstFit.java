package lk.sh.mp421642710;

import java.sql.*;

public class FirstFit {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mp";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "2001";

    private StringBuilder detailedLog; // Store allocation logs

    public FirstFit() {
        detailedLog = new StringBuilder(); // Initialize
    }

    public String runFirstFitAlgorithm() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            connection.setAutoCommit(false); // Start transaction

            // Fetch all unallocated jobs
            String jobQuery = "SELECT * FROM jobs WHERE allocated_partition IS NULL";
            ResultSet jobs = connection.createStatement().executeQuery(jobQuery);

            // Prepare SQL queries for updating partitions and jobs
            String partitionQuery = "SELECT * FROM partitions WHERE allocated = FALSE ORDER BY id";
            String updatePartitionQuery = "UPDATE partitions SET size = ?, allocated = ? WHERE id = ?";
            String updateJobQuery = "UPDATE jobs SET allocated_partition = ? WHERE job_id = ?";

            PreparedStatement updatePartitionStmt = connection.prepareStatement(updatePartitionQuery);
            PreparedStatement updateJobStmt = connection.prepareStatement(updateJobQuery);

            while (jobs.next()) {
                int jobId = jobs.getInt("job_id");
                int jobSize = jobs.getInt("job_size");
                boolean allocated = false;

                // Fetch available partitions
                ResultSet partitions = connection.createStatement().executeQuery(partitionQuery);

                while (partitions.next()) {
                    int partitionId = partitions.getInt("id");
                    int partitionSize = partitions.getInt("size");

                    // Check if the partition can fit the job
                    if (partitionSize >= jobSize) {
                        int updatedPartitionSize = partitionSize - jobSize;

                        // Update partition
                        updatePartitionStmt.setInt(1, updatedPartitionSize);
                        updatePartitionStmt.setBoolean(2, updatedPartitionSize == 0);
                        updatePartitionStmt.setInt(3, partitionId);
                        updatePartitionStmt.executeUpdate();

                        // Update job with allocated partition
                        updateJobStmt.setInt(1, partitionId);
                        updateJobStmt.setInt(2, jobId);
                        updateJobStmt.executeUpdate();

                        // Log the detailed allocation result
                        detailedLog.append("Job ").append(jobId)
                                .append(" allocated to Partition ").append(partitionId)
                                .append(" | Previous Partition Size: ").append(partitionSize)
                                .append(", Updated Partition Size: ").append(updatedPartitionSize)
                                .append("\n");

                        allocated = true;
                        break; // Exit partition loop once job is allocated
                    }
                }

                if (!allocated) {
                    detailedLog.append("Job ").append(jobId).append(" could not be allocated.\n");
                }

                partitions.close();
            }

            connection.commit(); // Commit all changes
            detailedLog.append("Memory Allocation Complete.");

        } catch (SQLException e) {
            e.printStackTrace();
            detailedLog.append("Error: ").append(e.getMessage());
        }

        return detailedLog.toString();
    }

    public String getDetailedLog() {
        return detailedLog.toString();
    }
}
