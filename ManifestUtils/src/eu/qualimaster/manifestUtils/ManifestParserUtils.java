package eu.qualimaster.manifestUtils;
import java.util.ArrayList;
import java.util.List;

import org.apache.ivy.plugins.resolver.IBiblioResolver;

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;

/**
 * Defines some helpful methods for the ManifestParser etc.
 * @author Patrik Pastuschek
 *
 */
public class ManifestParserUtils {

    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(ManifestParserUtils.class, 
            "eu.qualimaster.ManifestUtils");

    /**
     * Will create a List of IBiblioResolvers from a List of repositories (String).
     * @param repositories A List of Strings with the repositories.
     * @return A List of IBiblioResolvers which use the repositories.
     */
    public static List<IBiblioResolver> getResolver(List<String> repositories) {
        
        List<IBiblioResolver> result = new ArrayList<IBiblioResolver>();
        
        for (int i = 0; i < repositories.size(); i++) {
            
            IBiblioResolver resolver = new IBiblioResolver();
            resolver.setM2compatible(true);
            resolver.setUsepoms(true);
            resolver.setName("Repository#" + i);
            resolver.setRoot(repositories.get(i));
            resolver.setPattern("[organisation]/[module]/[revision]/[artifact]-[revision](-[classifier]).[ext]");
            resolver.setUseMavenMetadata(true);
            resolver.setCheckmodified(true);
            resolver.getSettings();
            resolver.setValidate(true);
            result.add(resolver);
            logger.info("REP#" + i + " = " + repositories.get(i));
            
        }
        
        return result;
        
    }
    
}
