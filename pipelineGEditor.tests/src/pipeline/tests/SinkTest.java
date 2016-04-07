/**
 */
package pipeline.tests;

import junit.textui.TestRunner;

import pipeline.PipelineFactory;
import pipeline.Sink;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Sink</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class SinkTest extends PipelineNodeTest
{

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public static void main(String[] args)
  {
		TestRunner.run(SinkTest.class);
	}

  /**
	 * Constructs a new Sink test case with the given name.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public SinkTest(String name)
  {
		super(name);
	}

  /**
	 * Returns the fixture for this Sink test case.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  protected Sink getFixture()
  {
		return (Sink)fixture;
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
		setFixture(PipelineFactory.eINSTANCE.createSink());
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

} //SinkTest
