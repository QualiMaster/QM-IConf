package eu.qualimaster.manifestUtils.data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Basic metadata information.
 * @author pastuschek
 *
 */
public class BaseMetaData {

    private String groupId;
    private String artifactId;
    private String lastUpdated;
    
    /**
     * Returns the groupId.
     * @return The groupId.
     */
    public String getGroupId() {
        return groupId;
    }
    
    /**
     * Sets the groupId.
     * @param groupId String.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    /**
     * Returns the artifactId.
     * @return The artifactId.
     */
    public String getArtifactId() {
        return artifactId;
    }
    
    /**
     * Sets the artifactId.
     * @param artifactId String.
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    /**
     * Returns the last update time.
     * @return Last update time.
     */
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Sets the last update time.
     * @param lastUpdated String.
     */
    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Generates the actual time stamp and stores it in lastUpdated.
     * @return The actual time in maven time stamp format.
     */
    public static String generateTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
    /**
     * Generates the actual time stamp and stores it in lastUpdated.
     * @return The actual time in maven time stamp format.
     */
    public static String generateTimestampWithDot() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd.HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    
}
