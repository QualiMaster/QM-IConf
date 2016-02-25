/**
 */
package pipeline.tests;

import junit.textui.TestRunner;

import pipeline.FamilyElement;
import pipeline.PipelineFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Family Element</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class FamilyElementTest extends ProcessingElementTest {

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static void main(String[] args) {
        TestRunner.run(FamilyElementTest.class);
    }

    /**
     * Constructs a new Family Element test case with the given name.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public FamilyElementTest(String name) {
        super(name);
    }

    /**
     * Returns the fixture for this Family Element test case.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected FamilyElement getFixture() {
        return (FamilyElement)fixture;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see junit.framework.TestCase#setUp()
     * @generated
     */
    @Override
    protected void setUp() throws Exception {
        setFixture(PipelineFactory.eINSTANCE.createFamilyElement());
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

} //FamilyElementTest
