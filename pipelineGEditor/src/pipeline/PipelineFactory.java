/**
 */
package pipeline;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see pipeline.PipelinePackage
 * @generated
 */
public interface PipelineFactory extends EFactory
{
  /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  PipelineFactory eINSTANCE = pipeline.impl.PipelineFactoryImpl.init();

  /**
     * Returns a new object of class '<em>Pipeline</em>'.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return a new object of class '<em>Pipeline</em>'.
     * @generated
     */
  Pipeline createPipeline();

  /**
     * Returns a new object of class '<em>Flow</em>'.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return a new object of class '<em>Flow</em>'.
     * @generated
     */
  Flow createFlow();

  /**
     * Returns a new object of class '<em>Source</em>'.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return a new object of class '<em>Source</em>'.
     * @generated
     */
  Source createSource();

  /**
     * Returns a new object of class '<em>Sink</em>'.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return a new object of class '<em>Sink</em>'.
     * @generated
     */
  Sink createSink();

  /**
     * Returns a new object of class '<em>Family Element</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Family Element</em>'.
     * @generated
     */
    FamilyElement createFamilyElement();

/**
     * Returns a new object of class '<em>Data Management Element</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Data Management Element</em>'.
     * @generated
     */
    DataManagementElement createDataManagementElement();

/**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
  PipelinePackage getPipelinePackage();

} //PipelineFactory
