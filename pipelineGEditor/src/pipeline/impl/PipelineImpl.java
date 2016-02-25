/**
 */
package pipeline.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.ecore.util.InternalEList;

import pipeline.Flow;
import pipeline.Node;
import pipeline.Pipeline;
import pipeline.PipelineNode;
import pipeline.PipelinePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Pipeline</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link pipeline.impl.PipelineImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getFlows <em>Flows</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getName <em>Name</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getNumworkers <em>Numworkers</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getArtifact <em>Artifact</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getConstraints <em>Constraints</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getDebug <em>Debug</em>}</li>
 *   <li>{@link pipeline.impl.PipelineImpl#getFastSerialization <em>Fast Serialization</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PipelineImpl extends EObjectImpl implements Pipeline
{
  /**
     * The cached value of the '{@link #getNodes() <em>Nodes</em>}' containment reference list.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @see #getNodes()
     * @generated
     * @ordered
     */
  protected EList<PipelineNode> nodes;

  /**
     * The cached value of the '{@link #getFlows() <em>Flows</em>}' containment reference list.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @see #getFlows()
     * @generated
     * @ordered
     */
  protected EList<Flow> flows;

  /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = "";

/**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

/**
     * The default value of the '{@link #getNumworkers() <em>Numworkers</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNumworkers()
     * @generated
     * @ordered
     */
    protected static final Integer NUMWORKERS_EDEFAULT = new Integer(1);

/**
     * The cached value of the '{@link #getNumworkers() <em>Numworkers</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getNumworkers()
     * @generated
     * @ordered
     */
    protected Integer numworkers = NUMWORKERS_EDEFAULT;

/**
     * The default value of the '{@link #getArtifact() <em>Artifact</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getArtifact()
     * @generated
     * @ordered
     */
	protected static final String ARTIFACT_EDEFAULT = "";

/**
     * The cached value of the '{@link #getArtifact() <em>Artifact</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getArtifact()
     * @generated
     * @ordered
     */
	protected String artifact = ARTIFACT_EDEFAULT;

/**
     * The default value of the '{@link #getConstraints() <em>Constraints</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConstraints()
     * @generated
     * @ordered
     */
    protected static final String CONSTRAINTS_EDEFAULT = "";

/**
     * The cached value of the '{@link #getConstraints() <em>Constraints</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getConstraints()
     * @generated
     * @ordered
     */
    protected String constraints = CONSTRAINTS_EDEFAULT;

/**
     * The default value of the '{@link #getDebug() <em>Debug</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDebug()
     * @generated
     * @ordered
     */
	protected static final Integer DEBUG_EDEFAULT = new Integer(1);

/**
     * The cached value of the '{@link #getDebug() <em>Debug</em>}' attribute.
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @see #getDebug()
     * @generated
     * @ordered
     */
	protected Integer debug = DEBUG_EDEFAULT;

/**
     * The default value of the '{@link #getFastSerialization() <em>Fast Serialization</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFastSerialization()
     * @generated
     * @ordered
     */
    protected static final Integer FAST_SERIALIZATION_EDEFAULT = new Integer(1);

/**
     * The cached value of the '{@link #getFastSerialization() <em>Fast Serialization</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getFastSerialization()
     * @generated
     * @ordered
     */
    protected Integer fastSerialization = FAST_SERIALIZATION_EDEFAULT;

/**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  protected PipelineImpl()
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
        return PipelinePackage.Literals.PIPELINE;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public EList<PipelineNode> getNodes()
  {
        if (nodes == null) {
            nodes = new EObjectContainmentEList<PipelineNode>(PipelineNode.class, this, PipelinePackage.PIPELINE__NODES);
        }
        return nodes;
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public EList<Flow> getFlows()
  {
        if (flows == null) {
            flows = new EObjectContainmentEList<Flow>(Flow.class, this, PipelinePackage.PIPELINE__FLOWS);
        }
        return flows;
    }

  /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getName() {
        return name;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__NAME, oldName, name));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Integer getNumworkers() {
        return numworkers;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setNumworkers(Integer newNumworkers) {
        Integer oldNumworkers = numworkers;
        numworkers = newNumworkers;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__NUMWORKERS, oldNumworkers, numworkers));
    }

/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public String getArtifact() {
        return artifact;
    }

/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * @generated
     */
	public void setArtifact(String newArtifact) {
        String oldArtifact = artifact;
        artifact = newArtifact;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__ARTIFACT, oldArtifact, artifact));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getConstraints() {
        return constraints;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setConstraints(String newConstraints) {
        String oldConstraints = constraints;
        constraints = newConstraints;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__CONSTRAINTS, oldConstraints, constraints));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Integer getDebug() {
        return debug;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setDebug(Integer newDebug) {
        Integer oldDebug = debug;
        debug = newDebug;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__DEBUG, oldDebug, debug));
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Integer getFastSerialization() {
        return fastSerialization;
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setFastSerialization(Integer newFastSerialization) {
        Integer oldFastSerialization = fastSerialization;
        fastSerialization = newFastSerialization;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, PipelinePackage.PIPELINE__FAST_SERIALIZATION, oldFastSerialization, fastSerialization));
    }

/**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
        switch (featureID) {
            case PipelinePackage.PIPELINE__NODES:
                return ((InternalEList<?>)getNodes()).basicRemove(otherEnd, msgs);
            case PipelinePackage.PIPELINE__FLOWS:
                return ((InternalEList<?>)getFlows()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
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
            case PipelinePackage.PIPELINE__NODES:
                return getNodes();
            case PipelinePackage.PIPELINE__FLOWS:
                return getFlows();
            case PipelinePackage.PIPELINE__NAME:
                return getName();
            case PipelinePackage.PIPELINE__NUMWORKERS:
                return getNumworkers();
            case PipelinePackage.PIPELINE__ARTIFACT:
                return getArtifact();
            case PipelinePackage.PIPELINE__CONSTRAINTS:
                return getConstraints();
            case PipelinePackage.PIPELINE__DEBUG:
                return getDebug();
            case PipelinePackage.PIPELINE__FAST_SERIALIZATION:
                return getFastSerialization();
        }
        return super.eGet(featureID, resolve, coreType);
    }

  /**
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  @SuppressWarnings("unchecked")
  @Override
  public void eSet(int featureID, Object newValue)
  {
        switch (featureID) {
            case PipelinePackage.PIPELINE__NODES:
                getNodes().clear();
                getNodes().addAll((Collection<? extends PipelineNode>)newValue);
                return;
            case PipelinePackage.PIPELINE__FLOWS:
                getFlows().clear();
                getFlows().addAll((Collection<? extends Flow>)newValue);
                return;
            case PipelinePackage.PIPELINE__NAME:
                setName((String)newValue);
                return;
            case PipelinePackage.PIPELINE__NUMWORKERS:
                setNumworkers((Integer)newValue);
                return;
            case PipelinePackage.PIPELINE__ARTIFACT:
                setArtifact((String)newValue);
                return;
            case PipelinePackage.PIPELINE__CONSTRAINTS:
                setConstraints((String)newValue);
                return;
            case PipelinePackage.PIPELINE__DEBUG:
                setDebug((Integer)newValue);
                return;
            case PipelinePackage.PIPELINE__FAST_SERIALIZATION:
                setFastSerialization((Integer)newValue);
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
            case PipelinePackage.PIPELINE__NODES:
                getNodes().clear();
                return;
            case PipelinePackage.PIPELINE__FLOWS:
                getFlows().clear();
                return;
            case PipelinePackage.PIPELINE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case PipelinePackage.PIPELINE__NUMWORKERS:
                setNumworkers(NUMWORKERS_EDEFAULT);
                return;
            case PipelinePackage.PIPELINE__ARTIFACT:
                setArtifact(ARTIFACT_EDEFAULT);
                return;
            case PipelinePackage.PIPELINE__CONSTRAINTS:
                setConstraints(CONSTRAINTS_EDEFAULT);
                return;
            case PipelinePackage.PIPELINE__DEBUG:
                setDebug(DEBUG_EDEFAULT);
                return;
            case PipelinePackage.PIPELINE__FAST_SERIALIZATION:
                setFastSerialization(FAST_SERIALIZATION_EDEFAULT);
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
            case PipelinePackage.PIPELINE__NODES:
                return nodes != null && !nodes.isEmpty();
            case PipelinePackage.PIPELINE__FLOWS:
                return flows != null && !flows.isEmpty();
            case PipelinePackage.PIPELINE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case PipelinePackage.PIPELINE__NUMWORKERS:
                return NUMWORKERS_EDEFAULT == null ? numworkers != null : !NUMWORKERS_EDEFAULT.equals(numworkers);
            case PipelinePackage.PIPELINE__ARTIFACT:
                return ARTIFACT_EDEFAULT == null ? artifact != null : !ARTIFACT_EDEFAULT.equals(artifact);
            case PipelinePackage.PIPELINE__CONSTRAINTS:
                return CONSTRAINTS_EDEFAULT == null ? constraints != null : !CONSTRAINTS_EDEFAULT.equals(constraints);
            case PipelinePackage.PIPELINE__DEBUG:
                return DEBUG_EDEFAULT == null ? debug != null : !DEBUG_EDEFAULT.equals(debug);
            case PipelinePackage.PIPELINE__FAST_SERIALIZATION:
                return FAST_SERIALIZATION_EDEFAULT == null ? fastSerialization != null : !FAST_SERIALIZATION_EDEFAULT.equals(fastSerialization);
        }
        return super.eIsSet(featureID);
    }

/**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (name: ");
        result.append(name);
        result.append(", numworkers: ");
        result.append(numworkers);
        result.append(", artifact: ");
        result.append(artifact);
        result.append(", constraints: ");
        result.append(constraints);
        result.append(", debug: ");
        result.append(debug);
        result.append(", fastSerialization: ");
        result.append(fastSerialization);
        result.append(')');
        return result.toString();
    }

} //PipelineImpl
