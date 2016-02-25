package eu.qualimaster.manifestUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.Item;

/**
 * Compares the parsed manifest with the actual underlying artifact.
 * @author Patrik Pastuschek
 *
 */
public class ManifestComparator {

    /**
     * Compares the parsed Family with the actual class.
     * @param family The parsed family.
     * @param cl The actual class.
     * @return true if the manifest and the class are alike. false otherwise.
     */
    public static ComparisonReport compare(Manifest family, Class<?> cl) {
        
        ComparisonReport report = null;
        boolean isSame = true;
        
        Collection<Algorithm> algs = family.getMembers();
        
        for (Algorithm alg : algs) {
            
            try {
                Method calculate = cl.getMethod("calculate", (Class<?>) null);
                
                Class<?>[] params = calculate.getParameterTypes();
                
                if (params.length == alg.getInput().iterator().next().getFields().size() 
                        + alg.getOutput().iterator().next().getFields().size()) {
                    
                    for (int i = 0; i < params.length; i++) {
                        
                        Iterator<Item> itemIt = alg.getInput().iterator();
                        Iterator<Field> inputIt = itemIt.next().getFields().iterator();
                        Iterator<Field> outputIt = itemIt.next().getFields().iterator();
                        
                        Field current = null;
                        
                        if (inputIt.hasNext()) {
                            current = inputIt.next();
                            
                            if (!current.getName().equals(params[i].getName())
                                    || !current.getFieldType().toString().equals(params[i].getClass().toString())) {
                                isSame = false;
                            }
                            
                        } else if (outputIt.hasNext()) {
                            current = outputIt.next();
                            
                            if (!current.getName().equals(params[i].getName())
                                    || !current.getFieldType().toString().equals(params[i].getClass().toString())) {
                                isSame = false;
                            }
                            
                        }
                        
                    }
                
                } else {
                    isSame = false;
                }
                
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
        if (isSame) {
            report = new ComparisonReport();
        }
        
        return report;        
        
    }
    
}
