/**
 */
package pipeline.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import pipeline.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see pipeline.PipelinePackage
 * @generated
 */
public class PipelineSwitch<T> extends Switch<T>
{
  /**
     * The cached model package
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  protected static PipelinePackage modelPackage;

  /**
     * Creates an instance of the switch.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @generated
     */
  public PipelineSwitch()
  {
        if (modelPackage == null) {
            modelPackage = PipelinePackage.eINSTANCE;
        }
    }

  /**
     * Checks whether this is a switch for the given package.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @parameter ePackage the package in question.
     * @return whether this is a switch for the given package.
     * @generated
     */
  @Override
  protected boolean isSwitchFor(EPackage ePackage)
  {
        return ePackage == modelPackage;
    }

  /**
     * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
     * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
     * @return the first non-null result returned by a <code>caseXXX</code> call.
     * @generated
     */
  @Override
  protected T doSwitch(int classifierID, EObject theEObject)
  {
        switch (classifierID) {
            case PipelinePackage.PIPELINE: {
                Pipeline pipeline = (Pipeline)theEObject;
                T result = casePipeline(pipeline);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.PIPELINE_ELEMENT: {
                PipelineElement pipelineElement = (PipelineElement)theEObject;
                T result = casePipelineElement(pipelineElement);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.FLOW: {
                Flow flow = (Flow)theEObject;
                T result = caseFlow(flow);
                if (result == null) result = casePipelineElement(flow);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.PIPELINE_NODE: {
                PipelineNode pipelineNode = (PipelineNode)theEObject;
                T result = casePipelineNode(pipelineNode);
                if (result == null) result = casePipelineElement(pipelineNode);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.SOURCE: {
                Source source = (Source)theEObject;
                T result = caseSource(source);
                if (result == null) result = casePipelineNode(source);
                if (result == null) result = casePipelineElement(source);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.SINK: {
                Sink sink = (Sink)theEObject;
                T result = caseSink(sink);
                if (result == null) result = casePipelineNode(sink);
                if (result == null) result = casePipelineElement(sink);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.PROCESSING_ELEMENT: {
                ProcessingElement processingElement = (ProcessingElement)theEObject;
                T result = caseProcessingElement(processingElement);
                if (result == null) result = casePipelineNode(processingElement);
                if (result == null) result = casePipelineElement(processingElement);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.FAMILY_ELEMENT: {
                FamilyElement familyElement = (FamilyElement)theEObject;
                T result = caseFamilyElement(familyElement);
                if (result == null) result = caseProcessingElement(familyElement);
                if (result == null) result = casePipelineNode(familyElement);
                if (result == null) result = casePipelineElement(familyElement);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            case PipelinePackage.DATA_MANAGEMENT_ELEMENT: {
                DataManagementElement dataManagementElement = (DataManagementElement)theEObject;
                T result = caseDataManagementElement(dataManagementElement);
                if (result == null) result = caseProcessingElement(dataManagementElement);
                if (result == null) result = casePipelineNode(dataManagementElement);
                if (result == null) result = casePipelineElement(dataManagementElement);
                if (result == null) result = defaultCase(theEObject);
                return result;
            }
            default: return defaultCase(theEObject);
        }
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Pipeline</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Pipeline</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
  public T casePipeline(Pipeline object)
  {
        return null;
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Element</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Element</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T casePipelineElement(PipelineElement object) {
        return null;
    }

/**
     * Returns the result of interpreting the object as an instance of '<em>Flow</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Flow</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
  public T caseFlow(Flow object)
  {
        return null;
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Node</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Node</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T casePipelineNode(PipelineNode object) {
        return null;
    }

/**
     * Returns the result of interpreting the object as an instance of '<em>Source</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Source</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
  public T caseSource(Source object)
  {
        return null;
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Sink</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Sink</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
  public T caseSink(Sink object)
  {
        return null;
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Processing Element</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Processing Element</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
  public T caseProcessingElement(ProcessingElement object)
  {
        return null;
    }

  /**
     * Returns the result of interpreting the object as an instance of '<em>Family Element</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Family Element</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseFamilyElement(FamilyElement object) {
        return null;
    }

/**
     * Returns the result of interpreting the object as an instance of '<em>Data Management Element</em>'.
     * <!-- begin-user-doc -->
     * This implementation returns null;
     * returning a non-null result will terminate the switch.
     * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>Data Management Element</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
     */
    public T caseDataManagementElement(DataManagementElement object) {
        return null;
    }

/**
     * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
     * <!-- begin-user-doc -->
   * This implementation returns null;
   * returning a non-null result will terminate the switch, but this is the last case anyway.
   * <!-- end-user-doc -->
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
  @Override
  public T defaultCase(EObject object)
  {
        return null;
    }

} //PipelineSwitch
