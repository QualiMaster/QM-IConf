package eu.qualimaster.manifestUtils.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents an Algorithm-Family.
 * @author pastuschek
 *
 */
public class Manifest extends Algorithm {

    /**A list of members, which are Algorithms.*/
    private List<Algorithm> members = new ArrayList<Algorithm>();
    /**The Id of the group of the artifact.*/
    private String groupId = null;
    /**The version of the artifact.*/
    private String version = null;
    /**The Id of the artifact.*/
    private String artifactId = null;
    /**The provider of this manifest.*/
    private String provider = null;
    /**The manifestType.*/
    private ManifestType mType = ManifestType.UNKNOWN;
    
    /**
     * Represents the type of manifest that was read.
     */
    public enum ManifestType {
        
        UNKNOWN(false, false, false), 
        SINGLE_JAVA(false, true, false), 
        SINGLE_HARDWARE(false, true, false), 
        STORMBASED(false, true, false), 
        MULTI_HARDWARE(false, true, false), 
        PIPELINE_SOURCE(true, false, false), 
        PIPELINE_SINK(false, false, true);
        
        private boolean isSource;
        private boolean isAlgorithm;
        private boolean isSink;
        
        /**
         * Creates a manifest type.
         * 
         * @param isSource whether this type can represent a source 
         * @param isAlgorithm whether this type can represent an algorithm
         * @param isSink whether this type can represent a sink
         */
        private ManifestType(boolean isSource, boolean isAlgorithm, boolean isSink) {
            this.isSource = isSource;
            this.isSink = isSink;
            this.isAlgorithm = isAlgorithm;
        }

        /**
         * Returns whether this type can represent a source.
         * 
         * @return <code>true</code> for source, <code>false</code> else
         */
        public boolean isSource() {
            return isSource;
        }

        /**
         * Returns whether this type can represent an algorithm.
         * 
         * @return <code>true</code> for algorithm, <code>false</code> else
         */
        public boolean isAlgorithm() {
            return isAlgorithm;
        }

        /**
         * Returns whether this type can represent a sink.
         * 
         * @return <code>true</code> for sink, <code>false</code> else
         */
        public boolean isSink() {
            return isSink;
        }
    }
    
    /**
     * Constructor of the family, needs a name.
     * @param name Name of the family as String.
     */
    public Manifest(String name) {
        super(name, null, null, null);
    }

    /**
     * Adds an Algorithm to the family.
     * @param member The Algorithm to add.
     */
    public void addMember(Algorithm member) {
        if (null != member) {
            this.members.add(member);
        }
    }
    
    /**
     * Returns a Collection of Algorithms, which are members of this family.
     * @return A Collection<Algroithm> with all member Algorithms.
     */
    public Collection<Algorithm> getMembers() {
        return Collections.unmodifiableList(this.members);
    }
     
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String result = "";
        result += ("Algorithm Family: " + this.name + "\n");
        result += super.toString();
        for (Algorithm alg : members) {        
            result += alg.toString();         
        } 
        return result;
    }
    
    /**
     * Returns true if an algorithm with given name exists inside this manifests members.
     * @param name The name of the algorithm.
     * @return true if the algorithm exists, false otherwise.
     */
    public boolean hasAlgorithm(String name) {
        
        boolean result = false;
        
        for (Algorithm a : this.members) {
            if (a.getName().equals(name)) {
                result = true;
            }
        }
        
        return result;
        
    }
    
    /**
     * Returns the member algorithm with given name or null if no member with given name exists.
     * @param name The name of target algorithm.
     * @return The algorithm or null.
     */
    public Algorithm getMember(String name) {
        
        Algorithm result = null;
        
        for (Algorithm a : this.members) {
            if (a.getName().equals(name)) {
                result = a;
            }
        }
        
        return result;
        
    }
    
    /**
     * Sets the provider of this manifest. Can be null.
     * @param provider The provider as String or null.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    /**
     * Sets the ManifestType for this manifest. Can not be null.
     * @param type The type of manifest.
     */
    public void setType(ManifestType type) {
        if (null != type) {
            this.mType = type;
        } else {
            throw new IllegalArgumentException("ManifestType can not be null!");
        }
    }
    
    /**
     * Returns the version of this manifest as String. Can be null.
     * @return The version as String.
     */
    public String getVersion() {
        return this.version;
    }
    
    /**
     * Returns the Id of the artifact as String. Can be null.
     * @return The Id of the artifact as String.
     */
    public String getArtifactId() {
        return this.artifactId;
    }
    
    /**
     * Returns the Id of the group as String. Can be null.
     * @return The Id of the group as String.
     */
    public String getGroupId() {
        return this.groupId;
    }
    
    /**
     * Returns the provider of this manifest as String. Can be null.
     * @return The provider as String.
     */
    public String getProvider() {
        return this.provider;
    }
    
    /**
     * Returns the type of manifest. Never null.
     * @return The type of manifest.
     */
    public ManifestType getType() {
        return this.mType;
    }
    
    
}
