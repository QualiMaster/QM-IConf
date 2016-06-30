/**
 */
package pipeline.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import pipeline.DataManagementElement;
import pipeline.FamilyElement;
import pipeline.Flow;
import pipeline.Node;
import pipeline.Pipeline;
import pipeline.PipelineElement;
import pipeline.PipelineFactory;
import pipeline.PipelineNode;
import pipeline.PipelinePackage;
import pipeline.ProcessingElement;
import pipeline.ReplaySink;
import pipeline.Sink;
import pipeline.Source;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PipelinePackageImpl extends EPackageImpl implements PipelinePackage
{
  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private EClass pipelineEClass = null;

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    private EClass pipelineElementEClass = null;

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private EClass flowEClass = null;

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    private EClass pipelineNodeEClass = null;

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private EClass sourceEClass = null;

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private EClass sinkEClass = null;

  /**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass replaySinkEClass = null;

		/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private EClass processingElementEClass = null;

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    private EClass familyElementEClass = null;

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    private EClass dataManagementElementEClass = null;

/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see pipeline.PipelinePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
  private PipelinePackageImpl()
  {
		super(eNS_URI, PipelineFactory.eINSTANCE);
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private static boolean isInited = false;

  /**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 * 
	 * <p>This method is used to initialize {@link PipelinePackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
  public static PipelinePackage init()
  {
		if (isInited) return (PipelinePackage)EPackage.Registry.INSTANCE.getEPackage(PipelinePackage.eNS_URI);

		// Obtain or create and register package
		PipelinePackageImpl thePipelinePackage = (PipelinePackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof PipelinePackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new PipelinePackageImpl());

		isInited = true;

		// Create package meta-data objects
		thePipelinePackage.createPackageContents();

		// Initialize created meta-data
		thePipelinePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		thePipelinePackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(PipelinePackage.eNS_URI, thePipelinePackage);
		return thePipelinePackage;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EClass getPipeline()
  {
		return pipelineEClass;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EReference getPipeline_Nodes()
  {
		return (EReference)pipelineEClass.getEStructuralFeatures().get(0);
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EReference getPipeline_Flows()
  {
		return (EReference)pipelineEClass.getEStructuralFeatures().get(1);
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipeline_Name() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(2);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipeline_Numworkers() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(3);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipeline_Artifact() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(4);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipeline_Constraints() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(5);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipeline_Debug() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(6);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipeline_FastSerialization() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(7);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipeline_IsSubPipeline() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(8);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipeline_SubPipelineFamily() {
		return (EAttribute)pipelineEClass.getEStructuralFeatures().get(9);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EClass getPipelineElement() {
		return pipelineElementEClass;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipelineElement_Name() {
		return (EAttribute)pipelineElementEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getPipelineElement_Constraints() {
		return (EAttribute)pipelineElementEClass.getEStructuralFeatures().get(1);
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EClass getFlow()
  {
		return flowEClass;
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EReference getFlow_Source()
  {
		return (EReference)flowEClass.getEStructuralFeatures().get(0);
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EReference getFlow_Destination() {
		return (EReference)flowEClass.getEStructuralFeatures().get(1);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getFlow_Grouping() {
		return (EAttribute)flowEClass.getEStructuralFeatures().get(2);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFlow_TupleType() {
		return (EAttribute)flowEClass.getEStructuralFeatures().get(3);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EClass getPipelineNode() {
		return pipelineNodeEClass;
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipelineNode_Parallelism() {
		return (EAttribute)pipelineNodeEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPipelineNode_Numtasks() {
		return (EAttribute)pipelineNodeEClass.getEStructuralFeatures().get(1);
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EClass getSource()
  {
		return sourceEClass;
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getSource_Source() {
		return (EAttribute)sourceEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EClass getSink()
  {
		return sinkEClass;
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getSink_Sink() {
		return (EAttribute)sinkEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getReplaySink() {
		return replaySinkEClass;
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public EClass getProcessingElement()
  {
		return processingElementEClass;
	}

  /**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EClass getFamilyElement() {
		return familyElementEClass;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getFamilyElement_Family() {
		return (EAttribute)familyElementEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFamilyElement_IsConnector() {
		return (EAttribute)familyElementEClass.getEStructuralFeatures().get(1);
	}

/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFamilyElement_Default() {
		return (EAttribute)familyElementEClass.getEStructuralFeatures().get(2);
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EClass getDataManagementElement() {
		return dataManagementElementEClass;
	}

/**
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 */
    public EAttribute getDataManagementElement_DataManagement() {
		return (EAttribute)dataManagementElementEClass.getEStructuralFeatures().get(0);
	}

/**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public PipelineFactory getPipelineFactory()
  {
		return (PipelineFactory)getEFactoryInstance();
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private boolean isCreated = false;

  /**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public void createPackageContents()
  {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		pipelineEClass = createEClass(PIPELINE);
		createEReference(pipelineEClass, PIPELINE__NODES);
		createEReference(pipelineEClass, PIPELINE__FLOWS);
		createEAttribute(pipelineEClass, PIPELINE__NAME);
		createEAttribute(pipelineEClass, PIPELINE__NUMWORKERS);
		createEAttribute(pipelineEClass, PIPELINE__ARTIFACT);
		createEAttribute(pipelineEClass, PIPELINE__CONSTRAINTS);
		createEAttribute(pipelineEClass, PIPELINE__DEBUG);
		createEAttribute(pipelineEClass, PIPELINE__FAST_SERIALIZATION);
		createEAttribute(pipelineEClass, PIPELINE__IS_SUB_PIPELINE);
		createEAttribute(pipelineEClass, PIPELINE__SUB_PIPELINE_FAMILY);

		pipelineElementEClass = createEClass(PIPELINE_ELEMENT);
		createEAttribute(pipelineElementEClass, PIPELINE_ELEMENT__NAME);
		createEAttribute(pipelineElementEClass, PIPELINE_ELEMENT__CONSTRAINTS);

		flowEClass = createEClass(FLOW);
		createEReference(flowEClass, FLOW__SOURCE);
		createEReference(flowEClass, FLOW__DESTINATION);
		createEAttribute(flowEClass, FLOW__GROUPING);
		createEAttribute(flowEClass, FLOW__TUPLE_TYPE);

		pipelineNodeEClass = createEClass(PIPELINE_NODE);
		createEAttribute(pipelineNodeEClass, PIPELINE_NODE__PARALLELISM);
		createEAttribute(pipelineNodeEClass, PIPELINE_NODE__NUMTASKS);

		sourceEClass = createEClass(SOURCE);
		createEAttribute(sourceEClass, SOURCE__SOURCE);

		sinkEClass = createEClass(SINK);
		createEAttribute(sinkEClass, SINK__SINK);

		replaySinkEClass = createEClass(REPLAY_SINK);

		processingElementEClass = createEClass(PROCESSING_ELEMENT);

		familyElementEClass = createEClass(FAMILY_ELEMENT);
		createEAttribute(familyElementEClass, FAMILY_ELEMENT__FAMILY);
		createEAttribute(familyElementEClass, FAMILY_ELEMENT__IS_CONNECTOR);
		createEAttribute(familyElementEClass, FAMILY_ELEMENT__DEFAULT);

		dataManagementElementEClass = createEClass(DATA_MANAGEMENT_ELEMENT);
		createEAttribute(dataManagementElementEClass, DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT);
	}

  /**
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  private boolean isInitialized = false;

  /**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  public void initializePackageContents()
  {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		flowEClass.getESuperTypes().add(this.getPipelineElement());
		pipelineNodeEClass.getESuperTypes().add(this.getPipelineElement());
		sourceEClass.getESuperTypes().add(this.getPipelineNode());
		sinkEClass.getESuperTypes().add(this.getPipelineNode());
		replaySinkEClass.getESuperTypes().add(this.getSink());
		processingElementEClass.getESuperTypes().add(this.getPipelineNode());
		familyElementEClass.getESuperTypes().add(this.getProcessingElement());
		dataManagementElementEClass.getESuperTypes().add(this.getProcessingElement());

		// Initialize classes and features; add operations and parameters
		initEClass(pipelineEClass, Pipeline.class, "Pipeline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPipeline_Nodes(), this.getPipelineNode(), null, "nodes", null, 0, -1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPipeline_Flows(), this.getFlow(), null, "flows", null, 0, -1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_Name(), ecorePackage.getEString(), "name", "", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_Numworkers(), ecorePackage.getEIntegerObject(), "numworkers", "1", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_Artifact(), ecorePackage.getEString(), "artifact", "", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_Constraints(), ecorePackage.getEString(), "constraints", "", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_Debug(), ecorePackage.getEIntegerObject(), "debug", "1", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_FastSerialization(), ecorePackage.getEIntegerObject(), "fastSerialization", "1", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_IsSubPipeline(), ecorePackage.getEBooleanObject(), "isSubPipeline", "false", 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipeline_SubPipelineFamily(), ecorePackage.getEString(), "subPipelineFamily", null, 0, 1, Pipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(pipelineElementEClass, PipelineElement.class, "PipelineElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPipelineElement_Name(), ecorePackage.getEString(), "name", "", 0, 1, PipelineElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipelineElement_Constraints(), ecorePackage.getEString(), "constraints", "", 0, 1, PipelineElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(flowEClass, Flow.class, "Flow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFlow_Source(), this.getPipelineElement(), null, "source", null, 0, 1, Flow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFlow_Destination(), this.getPipelineElement(), null, "destination", null, 0, 1, Flow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlow_Grouping(), ecorePackage.getEIntegerObject(), "grouping", "0", 0, 1, Flow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFlow_TupleType(), ecorePackage.getEString(), "tupleType", null, 0, 1, Flow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(pipelineNodeEClass, PipelineNode.class, "PipelineNode", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPipelineNode_Parallelism(), ecorePackage.getEIntegerObject(), "parallelism", "1", 0, 1, PipelineNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPipelineNode_Numtasks(), ecorePackage.getEIntegerObject(), "numtasks", "0", 0, 1, PipelineNode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sourceEClass, Source.class, "Source", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSource_Source(), ecorePackage.getEIntegerObject(), "source", "-1", 0, 1, Source.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(sinkEClass, Sink.class, "Sink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSink_Sink(), ecorePackage.getEIntegerObject(), "sink", "-1", 0, 1, Sink.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(replaySinkEClass, ReplaySink.class, "ReplaySink", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(processingElementEClass, ProcessingElement.class, "ProcessingElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(familyElementEClass, FamilyElement.class, "FamilyElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFamilyElement_Family(), ecorePackage.getEIntegerObject(), "family", "-1", 0, 1, FamilyElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFamilyElement_IsConnector(), ecorePackage.getEBooleanObject(), "isConnector", "false", 0, 1, FamilyElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFamilyElement_Default(), ecorePackage.getEString(), "default", null, 0, 1, FamilyElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataManagementElementEClass, DataManagementElement.class, "DataManagementElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataManagementElement_DataManagement(), ecorePackage.getEIntegerObject(), "dataManagement", "-1", 0, 1, DataManagementElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// gmf.diagram
		createGmfAnnotations();
		// gmf.node
		createGmf_1Annotations();
		// gmf.link
		createGmf_2Annotations();
	}

  /**
	 * Initializes the annotations for <b>gmf.diagram</b>.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected void createGmfAnnotations()
  {
		String source = "gmf.diagram";	
		addAnnotation
		  (pipelineEClass, 
		   source, 
		   new String[] {
		   });
	}

  /**
	 * Initializes the annotations for <b>gmf.node</b>.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected void createGmf_1Annotations()
  {
		String source = "gmf.node";	
		addAnnotation
		  (pipelineElementEClass, 
		   source, 
		   new String[] {
			 "label", "name",
			 "label.icon", "false",
			 "figure", "svg",
			 "label.placement", "external"
		   });	
		addAnnotation
		  (pipelineNodeEClass, 
		   source, 
		   new String[] {
		   });	
		addAnnotation
		  (sourceEClass, 
		   source, 
		   new String[] {
			 "figure", "svg",
			 "svg.uri", "platform:/plugin/pipelineGEditor/svg/source.svg",
			 "size", "60,60"
		   });	
		addAnnotation
		  (sinkEClass, 
		   source, 
		   new String[] {
			 "figure", "svg",
			 "svg.uri", "platform:/plugin/pipelineGEditor/svg/sink.svg",
			 "size", "55,55"
		   });	
		addAnnotation
		  (replaySinkEClass, 
		   source, 
		   new String[] {
			 "figure", "svg",
			 "svg.uri", "platform:/plugin/pipelineGEditor/svg/ReplaySink.svg",
			 "size", "55,55"
		   });	
		addAnnotation
		  (processingElementEClass, 
		   source, 
		   new String[] {
		   });	
		addAnnotation
		  (familyElementEClass, 
		   source, 
		   new String[] {
			 "figure", "svg",
			 "svg.uri", "platform:/plugin/pipelineGEditor/svg/familyelement.svg",
			 "size", "60,60"
		   });	
		addAnnotation
		  (dataManagementElementEClass, 
		   source, 
		   new String[] {
			 "figure", "svg",
			 "svg.uri", "platform:/plugin/pipelineGEditor/svg/datamanagement.svg",
			 "size", "60,60"
		   });
	}

  /**
	 * Initializes the annotations for <b>gmf.link</b>.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  protected void createGmf_2Annotations()
  {
		String source = "gmf.link";	
		addAnnotation
		  (flowEClass, 
		   source, 
		   new String[] {
			 "label", "name",
			 "source", "source",
			 "target", "destination",
			 "target.decoration", "arrow"
		   });
	}

} //PipelinePackageImpl
