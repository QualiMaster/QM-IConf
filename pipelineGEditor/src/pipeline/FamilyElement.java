/**
 */
package pipeline;

import org.eclipse.emf.common.util.EList;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Family Element</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link pipeline.FamilyElement#getFamily <em>Family</em>}</li>
 *   <li>{@link pipeline.FamilyElement#getIsConnector <em>Is Connector</em>}</li>
 *   <li>{@link pipeline.FamilyElement#getDefaultAlgorithm <em>Default Algorithm</em>}</li>
 *   <li>{@link pipeline.FamilyElement#getPermissibleParameters <em>Permissible Parameters</em>}</li>
 * </ul>
 * </p>
 *
 * @see pipeline.PipelinePackage#getFamilyElement()
 * @model annotation="gmf.node figure='svg' svg.uri='platform:/plugin/pipelineGEditor/svg/familyelement.svg' size='60,60'"
 * @generated
 */
public interface FamilyElement extends ProcessingElement {
    /**
	 * Returns the value of the '<em><b>Family</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Family</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Family</em>' attribute.
	 * @see #setFamily(Integer)
	 * @see pipeline.PipelinePackage#getFamilyElement_Family()
	 * @model default="-1"
	 * @generated
	 */
    Integer getFamily();

    /**
	 * Sets the value of the '{@link pipeline.FamilyElement#getFamily <em>Family</em>}' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Family</em>' attribute.
	 * @see #getFamily()
	 * @generated
	 */
    void setFamily(Integer value);

				/**
	 * Returns the value of the '<em><b>Is Connector</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Is Connector</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Connector</em>' attribute.
	 * @see #setIsConnector(Boolean)
	 * @see pipeline.PipelinePackage#getFamilyElement_IsConnector()
	 * @model default="false"
	 * @generated
	 */
	Boolean getIsConnector();

				/**
	 * Sets the value of the '{@link pipeline.FamilyElement#getIsConnector <em>Is Connector</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Connector</em>' attribute.
	 * @see #getIsConnector()
	 * @generated
	 */
	void setIsConnector(Boolean value);

				/**
	 * Returns the value of the '<em><b>Default Algorithm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Default Algorithm</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Default Algorithm</em>' attribute.
	 * @see #setDefaultAlgorithm(String)
	 * @see pipeline.PipelinePackage#getFamilyElement_DefaultAlgorithm()
	 * @model
	 * @generated
	 */
	String getDefaultAlgorithm();

				/**
	 * Sets the value of the '{@link pipeline.FamilyElement#getDefaultAlgorithm <em>Default Algorithm</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default Algorithm</em>' attribute.
	 * @see #getDefaultAlgorithm()
	 * @generated
	 */
	void setDefaultAlgorithm(String value);

				/**
	 * Returns the value of the '<em><b>Permissible Parameters</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Permissible Parameters</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Permissible Parameters</em>' attribute list.
	 * @see pipeline.PipelinePackage#getFamilyElement_PermissibleParameters()
	 * @model
	 * @generated
	 */
	EList<String> getPermissibleParameters();

} // FamilyElement
