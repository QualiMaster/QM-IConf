/**
 */
package pipeline.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import pipeline.Flow;
import pipeline.Node;
import pipeline.PipelineElement;
import pipeline.PipelinePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Flow</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.FlowImpl#getSource <em>Source</em>}</li>
 *   <li>{@link pipeline.impl.FlowImpl#getDestination <em>Destination</em>}</li>
 *   <li>{@link pipeline.impl.FlowImpl#getGrouping <em>Grouping</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class FlowImpl extends PipelineElementImpl implements Flow
{
  /**
     * The cached value of the '{@link #getSource() <em>Source</em>}' reference.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @see #getSource()
     * @generated
     * @ordered
     */
  protected PipelineElement source;

  /**
     * The cached value of the '{@link #getDestination() <em>Destination</em>}' reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getDestination()
     * @generated
     * @ordered
     */
    protected PipelineElement destination;

/**
     * The default value of the '{@link #getGrouping() <em>Grouping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGrouping()
     * @generated
     * @ordered
     */
    protected static final Integer GROUPING_EDEFAULT = new Integer(0);

/**
     * The cached value of the '{@link #getGrouping() <em>Grouping</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getGrouping()
     * @generated
     * @ordered
     */
    protected Integer grouping = GROUPING_EDEFAULT;

/**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  protected FlowImpl()
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
        return PipelinePackage.Literals.FLOW;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public PipelineElement getSource()
  {
        if (source != null && source.eIsProxy()) {
            InternalEObject oldSource = (InternalEObject)source;
            source = (PipelineElement)eResolveProxy(oldSource);
            if (source != oldSource) {
                if (eNotificationRequired())
                    eNotify(new ENotificationImpl(this, Notification.RESOLVE, PipelinePackage.FLOW__SOURCE, oldSource, source));
            }
        }
        return source;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public PipelineElement basicGetSource()
  {
        return source;
    }

  /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setSource(PipelineElement newSource) {
        PipelineElement oldSource = source;
        source = newSource;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FLOW__SOURCE, oldSource, source));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PipelineElement getDestination() {
        if (destination != null && destination.eIsProxy()) {
            InternalEObject oldDestination = (InternalEObject)destination;
            destination = (PipelineElement)eResolveProxy(oldDestination);
            if (destination != oldDestination) {
                if (eNotificationRequired())
                    eNotify(new ENotificationImpl(this, Notification.RESOLVE, PipelinePackage.FLOW__DESTINATION, oldDestination, destination));
            }
        }
        return destination;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public PipelineElement basicGetDestination() {
        return destination;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDestination(PipelineElement newDestination) {
        PipelineElement oldDestination = destination;
        destination = newDestination;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FLOW__DESTINATION, oldDestination, destination));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Integer getGrouping() {
        return grouping;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setGrouping(Integer newGrouping) {
        Integer oldGrouping = grouping;
        grouping = newGrouping;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.FLOW__GROUPING, oldGrouping, grouping));
    }

/**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
        switch (featureID) {
            case PipelinePackage.FLOW__SOURCE:
                if (resolve) return getSource();
                return basicGetSource();
            case PipelinePackage.FLOW__DESTINATION:
                if (resolve) return getDestination();
                return basicGetDestination();
            case PipelinePackage.FLOW__GROUPING:
                return getGrouping();
        }
        return super.eGet(featureID, resolve, coreType);
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public void eSet(int featureID, Object newValue)
  {
        switch (featureID) {
            case PipelinePackage.FLOW__SOURCE:
                setSource((PipelineElement)newValue);
                return;
            case PipelinePackage.FLOW__DESTINATION:
                setDestination((PipelineElement)newValue);
                return;
            case PipelinePackage.FLOW__GROUPING:
                setGrouping((Integer)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public void eUnset(int featureID)
  {
        switch (featureID) {
            case PipelinePackage.FLOW__SOURCE:
                setSource((PipelineElement)null);
                return;
            case PipelinePackage.FLOW__DESTINATION:
                setDestination((PipelineElement)null);
                return;
            case PipelinePackage.FLOW__GROUPING:
                setGrouping(GROUPING_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public boolean eIsSet(int featureID)
  {
        switch (featureID) {
            case PipelinePackage.FLOW__SOURCE:
                return source != null;
            case PipelinePackage.FLOW__DESTINATION:
                return destination != null;
            case PipelinePackage.FLOW__GROUPING:
                return GROUPING_EDEFAULT == null ? grouping != null : !GROUPING_EDEFAULT.equals(grouping);
        }
        return super.eIsSet(featureID);
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public String toString()
  {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (grouping: ");
        result.append(grouping);
        result.append(')');
        return result.toString();
    }

} //FlowImpl
