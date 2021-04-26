/**
 */
package pipeline.impl;

import org.eclipse.emf.ecore.EClass;

import pipeline.PipelinePackage;
import pipeline.ProcessingElement;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Processing Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public abstract class ProcessingElementImpl extends PipelineNodeImpl implements ProcessingElement
{
  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected ProcessingElementImpl()
  {
		super();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  @Override
  protected EClass eStaticClass()
  {
		return PipelinePackage.Literals.PROCESSING_ELEMENT;
	}

} //ProcessingElementImpl
