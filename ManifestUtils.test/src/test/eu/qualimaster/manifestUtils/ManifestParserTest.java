package test.eu.qualimaster.manifestUtils;
import java.io.File;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.ManifestParser;
import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Parameter;

/**
 * This JUNIT test class contains all tests required to assure the functionality of the ManifestParser.
 * @author Patrik Pastuschek
 *
 */

public class ManifestParserTest {

    //Paths to the test files. To be replaced with relative paths!
    private static final String MHA_FILE = "H:/Desktop/MP/manifest_test/new/mha.xml";
    private static final String PSPA_FILE = "H:/Desktop/MP/manifest_test/new/pspa.xml";
    private static final String SDA_FILE = "H:/Desktop/MP/manifest_test/new/sda.xml";
    private static final String SHA_FILE = "H:/Desktop/MP/manifest_test/new/sha.xml";
    private static final String SSA_FILE = "H:/Desktop/MP/manifest_test/new/ssa.xml";
    
    
    /**
     * Testcase for the Multi-Hardware-Algorithm.
     */
    @Test
    public void testMha() {
        
        System.out.println("##### MHA #####");
        System.out.println();
        
        ManifestParser mp = new ManifestParser();
        Manifest family = mp.parseFile(new File(MHA_FILE));
        
        Assert.assertTrue(family.getMembers().size() == 2);
        Algorithm first = family.getMembers().iterator().next();
        
        Assert.assertTrue(first.getName().equals("adaptiveFilter"));
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next().getName().equals("symbol"));
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next().getName().equals("symbol"));
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getParameters().iterator().next().getName().equals("window"));
        Assert.assertTrue(first.getParameters().iterator().next().getType() == Parameter.ParameterType.INTEGER);
        
        Iterator<Algorithm> it = family.getMembers().iterator();
        it.next();
        first = it.next();
        
        Assert.assertTrue(first.getName().equals("correlationMatrix"));
        
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next().getName().equals("symbol"));
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next().getName()
                .equals("identifier"));
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getParameters().iterator().next().getName().equals("window"));
        Assert.assertTrue(first.getParameters().iterator().next().getType() == Parameter.ParameterType.INTEGER);
        
        System.out.println(family.toString());
        
        System.out.println();
        
    }

    /**
     * Testcase for the Pipeline-Source / Sink-Adapter.
     */
    @Test
    public void testPspa() {
        
        System.out.println("##### PSPA #####");
        System.out.println();
        
        ManifestParser mp = new ManifestParser();
        Manifest family = mp.parseFile(new File(PSPA_FILE));
        
        System.out.println(family.toString());
        
        System.out.println();
    }
    
    /**
     * Testcase for the Stormbased-Algorithm.
     */
    @Test
    public void testSda() {
        
        System.out.println("##### SDA #####");
        System.out.println();
        
        ManifestParser mp = new ManifestParser();
        Manifest family = mp.parseFile(new File(SDA_FILE));
        
        Assert.assertTrue(family.getMembers().size() == 1);
        Algorithm first = family.getMembers().iterator().next();
        
        Assert.assertTrue(first.getName().equals("correlationMatrix"));
        
        System.out.println(family.toString());
        
        System.out.println();
        
    }
    
    /**
     * Testcase for the Single-Hardware-Algorithm.
     */
    @Test
    public void testSha() {
        
        System.out.println("##### SHA #####");
        System.out.println();
        
        ManifestParser mp = new ManifestParser();
        Manifest family = mp.parseFile(new File(SHA_FILE));
        
        Assert.assertTrue(family.getMembers().size() == 1);
        Algorithm first = family.getMembers().iterator().next();
        
        Assert.assertTrue(first.getName().equals("correlationMatrix"));
        
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next().getName().equals("symbol"));
        Assert.assertTrue(first.getInput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next().getName()
                .equals("identifier"));
        Assert.assertTrue(first.getOutput().iterator().next().getFields().iterator().next()
                .getFieldType() == FieldType.STRING);
        
        Assert.assertTrue(first.getParameters().iterator().next().getName().equals("window"));
        Assert.assertTrue(first.getParameters().iterator().next().getType() == Parameter.ParameterType.INTEGER);
        
        System.out.println(family.toString());
        
        System.out.println();
    }
    
    /**
     * Testcase for the Single-Software-Algorithm.
     */
    @Test
    public void testSsa() {
        
        System.out.println("##### SSA #####");
        System.out.println();
        
        ManifestParser mp = new ManifestParser();
        Manifest family = mp.parseFile(new File(SSA_FILE));
        
        Assert.assertTrue(family.getMembers().size() == 1);
        Algorithm first = family.getMembers().iterator().next();
        
        Assert.assertTrue(first.getName().equals("correlationMatrix"));
        
        System.out.println(family.toString());
        
        System.out.println();

    }
    
}
