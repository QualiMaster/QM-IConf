package eu.qualimaster.manifestUtils.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a metadata file of a maven repository.
 * @author pastuschek
 *
 */
public class Metadata extends BaseMetaData {

    private String releaseVersion;
    private List<String> versions = new ArrayList<String>();
    
    /**
     * Returns the release version.
     * @return The release version.
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }
    
    /**
     * Sets the release version.
     * @param releaseVersion String.
     */
    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }
    
    /**
     * Returns the versions.
     * @return A list of Strings.
     */
    public List<String> getVersions() {
        return versions;
    }
    
    /**
     * Adds a version.
     * @param version String.
     */
    public void addVersion(String version) {
        this.versions.add(version);
    }
    
}
