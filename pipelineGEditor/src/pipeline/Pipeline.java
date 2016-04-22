/**
 */
package pipeline;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pipeline</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.Pipeline#getNodes <em>Nodes</em>}</li>
 *   <li>{@link pipeline.Pipeline#getFlows <em>Flows</em>}</li>
 *   <li>{@link pipeline.Pipeline#getName <em>Name</em>}</li>
 *   <li>{@link pipeline.Pipeline#getNumworkers <em>Numworkers</em>}</li>
 *   <li>{@link pipeline.Pipeline#getArtifact <em>Artifact</em>}</li>
 *   <li>{@link pipeline.Pipeline#getConstraints <em>Constraints</em>}</li>
 *   <li>{@link pipeline.Pipeline#getDebug <em>Debug</em>}</li>
 *   <li>{@link pipeline.Pipeline#getFastSerialization <em>Fast Serialization</em>}</li>
 *   <li>{@link pipeline.Pipeline#getIsSubPipeline <em>Is Sub Pipeline</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getPipeline()
 * @model
 * @generated
 */
public interface Pipeline extends EObject
{
  /**
	 * Returns the value of the '<em><b>Nodes</b></em>' containment reference list.
	 * The list contents are of type {@link pipeline.PipelineNode}.
	 * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Nodes</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
	 * @return the value of the '<em>Nodes</em>' containment reference list.
	 * @see pipeline.PipelinePackage#getPipeline_Nodes()
	 * @model containment="true"
	 * @generated
	 */
  EList<PipelineNode> getNodes();

  /**
	 * Returns the value of the '<em><b>Flows</b></em>' containment reference list.
	 * The list contents are of type {@link pipeline.Flow}.
	 * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Flows</em>' containment reference list isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
	 * @return the value of the '<em>Flows</em>' containment reference list.
	 * @see pipeline.PipelinePackage#getPipeline_Flows()
	 * @model containment="true"
	 * @generated
	 */
  EList<Flow> getFlows();

/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see pipeline.PipelinePackage#getPipeline_Name()
	 * @model default=""
	 * @generated
	 */
    String getName();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
    void setName(String value);

/**
	 * Returns the value of the '<em><b>Numworkers</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Numworkers</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Numworkers</em>' attribute.
	 * @see #setNumworkers(Integer)
	 * @see pipeline.PipelinePackage#getPipeline_Numworkers()
	 * @model default="1"
	 * @generated
	 */
    Integer getNumworkers();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getNumworkers <em>Numworkers</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Numworkers</em>' attribute.
	 * @see #getNumworkers()
	 * @generated
	 */
    void setNumworkers(Integer value);

/**
	 * Returns the value of the '<em><b>Artifact</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Artifact</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Artifact</em>' attribute.
	 * @see #setArtifact(String)
	 * @see pipeline.PipelinePackage#getPipeline_Artifact()
	 * @model default=""
	 * @generated
	 */
	String getArtifact();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getArtifact <em>Artifact</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Artifact</em>' attribute.
	 * @see #getArtifact()
	 * @generated
	 */
	void setArtifact(String value);

/**
	 * Returns the value of the '<em><b>Constraints</b></em>' attribute.
	 * The default value is <code>""</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Constraints</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Constraints</em>' attribute.
	 * @see #setConstraints(String)
	 * @see pipeline.PipelinePackage#getPipeline_Constraints()
	 * @model default=""
	 * @generated
	 */
    String getConstraints();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getConstraints <em>Constraints</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Constraints</em>' attribute.
	 * @see #getConstraints()
	 * @generated
	 */
    void setConstraints(String value);

/**
	 * Returns the value of the '<em><b>Debug</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Debug</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Debug</em>' attribute.
	 * @see #setDebug(Integer)
	 * @see pipeline.PipelinePackage#getPipeline_Debug()
	 * @model default="1"
	 * @generated
	 */
    Integer getDebug();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getDebug <em>Debug</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Debug</em>' attribute.
	 * @see #getDebug()
	 * @generated
	 */
    void setDebug(Integer value);

/**
	 * Returns the value of the '<em><b>Fast Serialization</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Fast Serialization</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Fast Serialization</em>' attribute.
	 * @see #setFastSerialization(Integer)
	 * @see pipeline.PipelinePackage#getPipeline_FastSerialization()
	 * @model default="1"
	 * @generated
	 */
    Integer getFastSerialization();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getFastSerialization <em>Fast Serialization</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fast Serialization</em>' attribute.
	 * @see #getFastSerialization()
	 * @generated
	 */
    void setFastSerialization(Integer value);

/**
	 * Returns the value of the '<em><b>Is Sub Pipeline</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Sub Pipeline</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Sub Pipeline</em>' attribute.
	 * @see #setIsSubPipeline(Integer)
	 * @see pipeline.PipelinePackage#getPipeline_IsSubPipeline()
	 * @model default="1"
	 * @generated
	 */
	Integer getIsSubPipeline();

/**
	 * Sets the value of the '{@link pipeline.Pipeline#getIsSubPipeline <em>Is Sub Pipeline</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Sub Pipeline</em>' attribute.
	 * @see #getIsSubPipeline()
	 * @generated
	 */
	void setIsSubPipeline(Integer value);

} // Pipeline
