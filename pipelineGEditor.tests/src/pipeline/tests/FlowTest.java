/**
 */
package pipeline.tests;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import pipeline.Flow;
import pipeline.PipelineFactory;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Flow</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class FlowTest extends PipelineElementTest
{

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public static void main(String[] args)
  {
        TestRunner.run(FlowTest.class);
    }

  /**
     * Constructs a new Flow test case with the given name.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public FlowTest(String name)
  {
        super(name);
    }

  /**
     * Returns the fixture for this Flow test case.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
protected Flow getFixture()
  {
        return (Flow)fixture;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @see junit.framework.TestCase#setUp()
     * @generated
     */
  @Override
  protected void setUp() throws Exception
  {
        setFixture(PipelineFactory.eINSTANCE.createFlow());
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @see junit.framework.TestCase#tearDown()
     * @generated
     */
  @Override
  protected void tearDown() throws Exception
  {
        setFixture(null);
    }

} //FlowTest
