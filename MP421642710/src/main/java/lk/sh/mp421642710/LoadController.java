package lk.sh.mp421642710;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LoadController {

    @FXML
    private TextArea output;


    @FXML
    void allData(ActionEvent event) {
        try {
            // Step 1: Run First Fit Algorithm
            FirstFit allocation = new FirstFit();
            String allocationResult = allocation.runFirstFitAlgorithm();

            // Step 2: Fetch Updated Jobs Data with Partition Details
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/mp", "root", "2001");
            Statement stmt = conn.createStatement();
            String query = "SELECT j.job_id, j.job_size, j.allocated_partition, " +
                    "p.id AS partition_id, p.size AS updated_partition_size, " +
                    "(p.size + j.job_size) AS previous_partition_size " +
                    "FROM jobs j " +
                    "LEFT JOIN partitions p ON j.allocated_partition = p.id";

            ResultSet rs = stmt.executeQuery(query);

            // Combine allocation log and updated job data with partition details
            StringBuilder data = new StringBuilder(allocationResult)
                    .append("\n\nUpdated Jobs Data:\n");

            while (rs.next()) {
                int jobId = rs.getInt("job_id");
                int jobSize = rs.getInt("job_size");
                int allocatedPartition = rs.getInt("allocated_partition");
                int previousPartitionSize = rs.getInt("previous_partition_size");
                int updatedPartitionSize = rs.getInt("updated_partition_size");

                if (allocatedPartition != 0) { // Only if partition is allocated
                    data.append("Job ID: ").append(jobId)
                            .append(", Job Size: ").append(jobSize)
                            .append(", Allocated Partition: ").append(allocatedPartition)
                            .append(" | Previous Partition Size: ").append(previousPartitionSize)
                            .append(", Updated Partition Size: ").append(updatedPartitionSize)
                            .append("\n");
                } else {
                    data.append("Job ID: ").append(jobId)
                            .append(", Job Size: ").append(jobSize)
                            .append(", Allocated Partition: Not Allocated\n");
                }
            }

            // Close connections
            rs.close();
            stmt.close();
            conn.close();

            // Display the combined result
            output.setText(data.toString());
        } catch (Exception e) {
            output.setText("Error: " + e.getMessage());
        }
    }


    @FXML
    void startAllocation(ActionEvent event) {
        try {
            FirstFit allocation = new FirstFit();
            String result = allocation.runFirstFitAlgorithm();
            output.setText(result);
        } catch (Exception e) {
            output.setText("Error: " + e.getMessage());
        }
    }
}
