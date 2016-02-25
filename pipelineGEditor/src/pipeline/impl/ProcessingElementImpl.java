/**
 */
package pipeline.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import pipeline.Flow;
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
