package eu.qualimaster.manifestUtils;

import org.apache.ivy.ant.IvyConvertPom;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.tools.ant.BuildException;

/**
 * A simple wrapper to provide the correct settings for the PomConverter.
 * @author pastuschek
 *
 */
public class IvySettingsWrapperTask extends IvyConvertPom {

    private IvySettings settings;
    
    @Override
    protected IvySettings getSettings() {
        return settings;
    }
    
    @Override
    public void doExecute() throws BuildException {
        super.doExecute();
    }
    
    /**
     * Setter for the IvySettings. These will be used for the PomConverter!
     * @param settings The ivySettings.
     */
    public void setSettings(IvySettings settings) {
        this.settings = settings;
    }

}
