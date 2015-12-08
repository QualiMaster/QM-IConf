package test.eu.qualimaster.manifestUtils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import test.eu.qualimaster.manifestUtils.data.AlgorithmTest;
import test.eu.qualimaster.manifestUtils.data.FieldTest;
import test.eu.qualimaster.manifestUtils.data.ItemTest;
import test.eu.qualimaster.manifestUtils.data.ManifestTest;
import test.eu.qualimaster.manifestUtils.data.ParameterTest;

/**
 * Test suite for all Manifest Util tests.
 *
 * @author Sass
 */
@RunWith(Suite.class)
@SuiteClasses({ FieldTest.class, ItemTest.class, 
    ParameterTest.class, AlgorithmTest.class, ManifestTest.class }) // TODO replace by real tests
public class AllTests {

}
