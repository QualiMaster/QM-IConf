/**
 */
package pipeline.tests;

import junit.textui.TestRunner;

import pipeline.DataManagementElement;
import pipeline.PipelineFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Data Management Element</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class DataManagementElementTest extends ProcessingElementTest {

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static void main(String[] args) {
        TestRunner.run(DataManagementElementTest.class);
    }

    /**
     * Constructs a new Data Management Element test case with the given name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DataManagementElementTest(String name) {
        super(name);
    }

    /**
     * Returns the fixture for this Data Management Element test case.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected DataManagementElement getFixture() {
        return (DataManagementElement)fixture;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see junit.framework.TestCase#setUp()
     * @generated
     */
    @Override
    protected void setUp() throws Exception {
        setFixture(PipelineFactory.eINSTANCE.createDataManagementElement());
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see junit.framework.TestCase#tearDown()
     * @generated
     */
    @Override
    protected void tearDown() throws Exception {
        setFixture(null);
    }

} //DataManagementElementTest
