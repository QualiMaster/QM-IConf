/**
 */
package pipeline;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see pipeline.PipelineFactory
 * @model kind="package"
 * @generated
 */
public interface PipelinePackage extends EPackage
{
  /**
	 * The package name.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  String eNAME = "pipeline";

  /**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  String eNS_URI = "pipeline";

  /**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  String eNS_PREFIX = "pipeline";

  /**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 */
  PipelinePackage eINSTANCE = pipeline.impl.PipelinePackageImpl.init();

  /**
	 * The meta object id for the '{@link pipeline.impl.PipelineImpl <em>Pipeline</em>}' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see pipeline.impl.PipelineImpl
	 * @see pipeline.impl.PipelinePackageImpl#getPipeline()
	 * @generated
	 */
  int PIPELINE = 0;

  /**
	 * The feature id for the '<em><b>Nodes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int PIPELINE__NODES = 0;

  /**
	 * The feature id for the '<em><b>Flows</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int PIPELINE__FLOWS = 1;

  /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE__NAME = 2;

/**
	 * The feature id for the '<em><b>Numworkers</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE__NUMWORKERS = 3;

/**
	 * The feature id for the '<em><b>Artifact</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE__ARTIFACT = 4;

/**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE__CONSTRAINTS = 5;

/**
	 * The feature id for the '<em><b>Debug</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE__DEBUG = 6;

/**
	 * The feature id for the '<em><b>Fast Serialization</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE__FAST_SERIALIZATION = 7;

/**
	 * The feature id for the '<em><b>Is Sub Pipeline</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE__IS_SUB_PIPELINE = 8;

/**
	 * The feature id for the '<em><b>Sub Pipeline Family</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE__SUB_PIPELINE_FAMILY = 9;

/**
	 * The number of structural features of the '<em>Pipeline</em>' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int PIPELINE_FEATURE_COUNT = 10;

  /**
	 * The meta object id for the '{@link pipeline.impl.PipelineElementImpl <em>Element</em>}' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see pipeline.impl.PipelineElementImpl
	 * @see pipeline.impl.PipelinePackageImpl#getPipelineElement()
	 * @generated
	 */
    int PIPELINE_ELEMENT = 1;

/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_ELEMENT__NAME = 0;

/**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_ELEMENT__CONSTRAINTS = 1;

/**
	 * The number of structural features of the '<em>Element</em>' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_ELEMENT_FEATURE_COUNT = 2;

/**
	 * The meta object id for the '{@link pipeline.impl.FlowImpl <em>Flow</em>}' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see pipeline.impl.FlowImpl
	 * @see pipeline.impl.PipelinePackageImpl#getFlow()
	 * @generated
	 */
  int FLOW = 2;

  /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int FLOW__NAME = PIPELINE_ELEMENT__NAME;

  /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FLOW__CONSTRAINTS = PIPELINE_ELEMENT__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int FLOW__SOURCE = PIPELINE_ELEMENT_FEATURE_COUNT + 0;

  /**
	 * The feature id for the '<em><b>Destination</b></em>' reference.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FLOW__DESTINATION = PIPELINE_ELEMENT_FEATURE_COUNT + 1;

/**
	 * The feature id for the '<em><b>Grouping</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FLOW__GROUPING = PIPELINE_ELEMENT_FEATURE_COUNT + 2;

/**
	 * The feature id for the '<em><b>Tuple Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FLOW__TUPLE_TYPE = PIPELINE_ELEMENT_FEATURE_COUNT + 3;

/**
	 * The number of structural features of the '<em>Flow</em>' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int FLOW_FEATURE_COUNT = PIPELINE_ELEMENT_FEATURE_COUNT + 4;

  /**
	 * The meta object id for the '{@link pipeline.impl.PipelineNodeImpl <em>Node</em>}' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see pipeline.impl.PipelineNodeImpl
	 * @see pipeline.impl.PipelinePackageImpl#getPipelineNode()
	 * @generated
	 */
    int PIPELINE_NODE = 3;

/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_NODE__NAME = PIPELINE_ELEMENT__NAME;

/**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_NODE__CONSTRAINTS = PIPELINE_ELEMENT__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE_NODE__PARALLELISM = PIPELINE_ELEMENT_FEATURE_COUNT + 0;

/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PIPELINE_NODE__NUMTASKS = PIPELINE_ELEMENT_FEATURE_COUNT + 1;

/**
	 * The number of structural features of the '<em>Node</em>' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PIPELINE_NODE_FEATURE_COUNT = PIPELINE_ELEMENT_FEATURE_COUNT + 2;

/**
	 * The meta object id for the '{@link pipeline.impl.SourceImpl <em>Source</em>}' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see pipeline.impl.SourceImpl
	 * @see pipeline.impl.PipelinePackageImpl#getSource()
	 * @generated
	 */
  int SOURCE = 4;

  /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int SOURCE__NAME = PIPELINE_NODE__NAME;

  /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int SOURCE__CONSTRAINTS = PIPELINE_NODE__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE__PARALLELISM = PIPELINE_NODE__PARALLELISM;

/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE__NUMTASKS = PIPELINE_NODE__NUMTASKS;

/**
	 * The feature id for the '<em><b>Source</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int SOURCE__SOURCE = PIPELINE_NODE_FEATURE_COUNT + 0;

/**
	 * The feature id for the '<em><b>Permissible Parameters</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SOURCE__PERMISSIBLE_PARAMETERS = PIPELINE_NODE_FEATURE_COUNT + 1;

/**
	 * The number of structural features of the '<em>Source</em>' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int SOURCE_FEATURE_COUNT = PIPELINE_NODE_FEATURE_COUNT + 2;

  /**
	 * The meta object id for the '{@link pipeline.impl.SinkImpl <em>Sink</em>}' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see pipeline.impl.SinkImpl
	 * @see pipeline.impl.PipelinePackageImpl#getSink()
	 * @generated
	 */
  int SINK = 5;

  /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int SINK__NAME = PIPELINE_NODE__NAME;

  /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int SINK__CONSTRAINTS = PIPELINE_NODE__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SINK__PARALLELISM = PIPELINE_NODE__PARALLELISM;

/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SINK__NUMTASKS = PIPELINE_NODE__NUMTASKS;

/**
	 * The feature id for the '<em><b>Sink</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int SINK__SINK = PIPELINE_NODE_FEATURE_COUNT + 0;

/**
	 * The feature id for the '<em><b>Permissible Parameters</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SINK__PERMISSIBLE_PARAMETERS = PIPELINE_NODE_FEATURE_COUNT + 1;

/**
	 * The number of structural features of the '<em>Sink</em>' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int SINK_FEATURE_COUNT = PIPELINE_NODE_FEATURE_COUNT + 2;

  /**
	 * The meta object id for the '{@link pipeline.impl.ReplaySinkImpl <em>Replay Sink</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see pipeline.impl.ReplaySinkImpl
	 * @see pipeline.impl.PipelinePackageImpl#getReplaySink()
	 * @generated
	 */
	int REPLAY_SINK = 6;

/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__NAME = SINK__NAME;

/**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__CONSTRAINTS = SINK__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__PARALLELISM = SINK__PARALLELISM;

/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__NUMTASKS = SINK__NUMTASKS;

/**
	 * The feature id for the '<em><b>Sink</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__SINK = SINK__SINK;

/**
	 * The feature id for the '<em><b>Permissible Parameters</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK__PERMISSIBLE_PARAMETERS = SINK__PERMISSIBLE_PARAMETERS;

/**
	 * The number of structural features of the '<em>Replay Sink</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REPLAY_SINK_FEATURE_COUNT = SINK_FEATURE_COUNT + 0;

		/**
	 * The meta object id for the '{@link pipeline.impl.ProcessingElementImpl <em>Processing Element</em>}' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @see pipeline.impl.ProcessingElementImpl
	 * @see pipeline.impl.PipelinePackageImpl#getProcessingElement()
	 * @generated
	 */
  int PROCESSING_ELEMENT = 7;

  /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int PROCESSING_ELEMENT__NAME = PIPELINE_NODE__NAME;

  /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int PROCESSING_ELEMENT__CONSTRAINTS = PIPELINE_NODE__CONSTRAINTS;

/**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROCESSING_ELEMENT__PARALLELISM = PIPELINE_NODE__PARALLELISM;

/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PROCESSING_ELEMENT__NUMTASKS = PIPELINE_NODE__NUMTASKS;

/**
	 * The number of structural features of the '<em>Processing Element</em>' class.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
  int PROCESSING_ELEMENT_FEATURE_COUNT = PIPELINE_NODE_FEATURE_COUNT + 0;


  /**
	 * The meta object id for the '{@link pipeline.impl.FamilyElementImpl <em>Family Element</em>}' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see pipeline.impl.FamilyElementImpl
	 * @see pipeline.impl.PipelinePackageImpl#getFamilyElement()
	 * @generated
	 */
    int FAMILY_ELEMENT = 8;

        /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FAMILY_ELEMENT__NAME = PROCESSING_ELEMENT__NAME;

        /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FAMILY_ELEMENT__CONSTRAINTS = PROCESSING_ELEMENT__CONSTRAINTS;

        /**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FAMILY_ELEMENT__PARALLELISM = PROCESSING_ELEMENT__PARALLELISM;

								/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FAMILY_ELEMENT__NUMTASKS = PROCESSING_ELEMENT__NUMTASKS;

								/**
	 * The feature id for the '<em><b>Family</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FAMILY_ELEMENT__FAMILY = PROCESSING_ELEMENT_FEATURE_COUNT + 0;

        /**
	 * The feature id for the '<em><b>Is Connector</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FAMILY_ELEMENT__IS_CONNECTOR = PROCESSING_ELEMENT_FEATURE_COUNT + 1;

								/**
	 * The feature id for the '<em><b>Default Algorithm</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FAMILY_ELEMENT__DEFAULT_ALGORITHM = PROCESSING_ELEMENT_FEATURE_COUNT + 2;

								/**
	 * The feature id for the '<em><b>Permissible Parameters</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS = PROCESSING_ELEMENT_FEATURE_COUNT + 3;

								/**
	 * The number of structural features of the '<em>Family Element</em>' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int FAMILY_ELEMENT_FEATURE_COUNT = PROCESSING_ELEMENT_FEATURE_COUNT + 4;

        /**
	 * The meta object id for the '{@link pipeline.impl.DataManagementElementImpl <em>Data Management Element</em>}' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @see pipeline.impl.DataManagementElementImpl
	 * @see pipeline.impl.PipelinePackageImpl#getDataManagementElement()
	 * @generated
	 */
    int DATA_MANAGEMENT_ELEMENT = 9;

        /**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int DATA_MANAGEMENT_ELEMENT__NAME = PROCESSING_ELEMENT__NAME;

        /**
	 * The feature id for the '<em><b>Constraints</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int DATA_MANAGEMENT_ELEMENT__CONSTRAINTS = PROCESSING_ELEMENT__CONSTRAINTS;

        /**
	 * The feature id for the '<em><b>Parallelism</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_MANAGEMENT_ELEMENT__PARALLELISM = PROCESSING_ELEMENT__PARALLELISM;

								/**
	 * The feature id for the '<em><b>Numtasks</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_MANAGEMENT_ELEMENT__NUMTASKS = PROCESSING_ELEMENT__NUMTASKS;

								/**
	 * The feature id for the '<em><b>Data Management</b></em>' attribute.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT = PROCESSING_ELEMENT_FEATURE_COUNT + 0;

        /**
	 * The number of structural features of the '<em>Data Management Element</em>' class.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
    int DATA_MANAGEMENT_ELEMENT_FEATURE_COUNT = PROCESSING_ELEMENT_FEATURE_COUNT + 1;

/**
	 * Returns the meta object for class '{@link pipeline.Pipeline <em>Pipeline</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Pipeline</em>'.
	 * @see pipeline.Pipeline
	 * @generated
	 */
  EClass getPipeline();

  /**
	 * Returns the meta object for the containment reference list '{@link pipeline.Pipeline#getNodes <em>Nodes</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Nodes</em>'.
	 * @see pipeline.Pipeline#getNodes()
	 * @see #getPipeline()
	 * @generated
	 */
  EReference getPipeline_Nodes();

  /**
	 * Returns the meta object for the containment reference list '{@link pipeline.Pipeline#getFlows <em>Flows</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Flows</em>'.
	 * @see pipeline.Pipeline#getFlows()
	 * @see #getPipeline()
	 * @generated
	 */
  EReference getPipeline_Flows();

  /**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see pipeline.Pipeline#getName()
	 * @see #getPipeline()
	 * @generated
	 */
    EAttribute getPipeline_Name();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getNumworkers <em>Numworkers</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Numworkers</em>'.
	 * @see pipeline.Pipeline#getNumworkers()
	 * @see #getPipeline()
	 * @generated
	 */
    EAttribute getPipeline_Numworkers();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getArtifact <em>Artifact</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Artifact</em>'.
	 * @see pipeline.Pipeline#getArtifact()
	 * @see #getPipeline()
	 * @generated
	 */
	EAttribute getPipeline_Artifact();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getConstraints <em>Constraints</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Constraints</em>'.
	 * @see pipeline.Pipeline#getConstraints()
	 * @see #getPipeline()
	 * @generated
	 */
    EAttribute getPipeline_Constraints();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getDebug <em>Debug</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Debug</em>'.
	 * @see pipeline.Pipeline#getDebug()
	 * @see #getPipeline()
	 * @generated
	 */
	EAttribute getPipeline_Debug();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getFastSerialization <em>Fast Serialization</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Fast Serialization</em>'.
	 * @see pipeline.Pipeline#getFastSerialization()
	 * @see #getPipeline()
	 * @generated
	 */
    EAttribute getPipeline_FastSerialization();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getIsSubPipeline <em>Is Sub Pipeline</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Sub Pipeline</em>'.
	 * @see pipeline.Pipeline#getIsSubPipeline()
	 * @see #getPipeline()
	 * @generated
	 */
	EAttribute getPipeline_IsSubPipeline();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Pipeline#getSubPipelineFamily <em>Sub Pipeline Family</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sub Pipeline Family</em>'.
	 * @see pipeline.Pipeline#getSubPipelineFamily()
	 * @see #getPipeline()
	 * @generated
	 */
	EAttribute getPipeline_SubPipelineFamily();

/**
	 * Returns the meta object for class '{@link pipeline.PipelineElement <em>Element</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Element</em>'.
	 * @see pipeline.PipelineElement
	 * @generated
	 */
    EClass getPipelineElement();

/**
	 * Returns the meta object for the attribute '{@link pipeline.PipelineElement#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see pipeline.PipelineElement#getName()
	 * @see #getPipelineElement()
	 * @generated
	 */
    EAttribute getPipelineElement_Name();

/**
	 * Returns the meta object for the attribute '{@link pipeline.PipelineElement#getConstraints <em>Constraints</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Constraints</em>'.
	 * @see pipeline.PipelineElement#getConstraints()
	 * @see #getPipelineElement()
	 * @generated
	 */
    EAttribute getPipelineElement_Constraints();

/**
	 * Returns the meta object for class '{@link pipeline.Flow <em>Flow</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Flow</em>'.
	 * @see pipeline.Flow
	 * @generated
	 */
  EClass getFlow();

  /**
	 * Returns the meta object for the reference '{@link pipeline.Flow#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Source</em>'.
	 * @see pipeline.Flow#getSource()
	 * @see #getFlow()
	 * @generated
	 */
  EReference getFlow_Source();

  /**
	 * Returns the meta object for the reference '{@link pipeline.Flow#getDestination <em>Destination</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Destination</em>'.
	 * @see pipeline.Flow#getDestination()
	 * @see #getFlow()
	 * @generated
	 */
    EReference getFlow_Destination();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Flow#getGrouping <em>Grouping</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Grouping</em>'.
	 * @see pipeline.Flow#getGrouping()
	 * @see #getFlow()
	 * @generated
	 */
    EAttribute getFlow_Grouping();

/**
	 * Returns the meta object for the attribute '{@link pipeline.Flow#getTupleType <em>Tuple Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tuple Type</em>'.
	 * @see pipeline.Flow#getTupleType()
	 * @see #getFlow()
	 * @generated
	 */
	EAttribute getFlow_TupleType();

/**
	 * Returns the meta object for class '{@link pipeline.PipelineNode <em>Node</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Node</em>'.
	 * @see pipeline.PipelineNode
	 * @generated
	 */
    EClass getPipelineNode();

/**
	 * Returns the meta object for the attribute '{@link pipeline.PipelineNode#getParallelism <em>Parallelism</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parallelism</em>'.
	 * @see pipeline.PipelineNode#getParallelism()
	 * @see #getPipelineNode()
	 * @generated
	 */
	EAttribute getPipelineNode_Parallelism();

/**
	 * Returns the meta object for the attribute '{@link pipeline.PipelineNode#getNumtasks <em>Numtasks</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Numtasks</em>'.
	 * @see pipeline.PipelineNode#getNumtasks()
	 * @see #getPipelineNode()
	 * @generated
	 */
	EAttribute getPipelineNode_Numtasks();

/**
	 * Returns the meta object for class '{@link pipeline.Source <em>Source</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Source</em>'.
	 * @see pipeline.Source
	 * @generated
	 */
  EClass getSource();

  /**
	 * Returns the meta object for the attribute '{@link pipeline.Source#getSource <em>Source</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Source</em>'.
	 * @see pipeline.Source#getSource()
	 * @see #getSource()
	 * @generated
	 */
    EAttribute getSource_Source();

/**
	 * Returns the meta object for the attribute list '{@link pipeline.Source#getPermissibleParameters <em>Permissible Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Permissible Parameters</em>'.
	 * @see pipeline.Source#getPermissibleParameters()
	 * @see #getSource()
	 * @generated
	 */
	EAttribute getSource_PermissibleParameters();

/**
	 * Returns the meta object for class '{@link pipeline.Sink <em>Sink</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Sink</em>'.
	 * @see pipeline.Sink
	 * @generated
	 */
  EClass getSink();

  /**
	 * Returns the meta object for the attribute '{@link pipeline.Sink#getSink <em>Sink</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sink</em>'.
	 * @see pipeline.Sink#getSink()
	 * @see #getSink()
	 * @generated
	 */
    EAttribute getSink_Sink();

/**
	 * Returns the meta object for the attribute list '{@link pipeline.Sink#getPermissibleParameters <em>Permissible Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Permissible Parameters</em>'.
	 * @see pipeline.Sink#getPermissibleParameters()
	 * @see #getSink()
	 * @generated
	 */
	EAttribute getSink_PermissibleParameters();

/**
	 * Returns the meta object for class '{@link pipeline.ReplaySink <em>Replay Sink</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Replay Sink</em>'.
	 * @see pipeline.ReplaySink
	 * @generated
	 */
	EClass getReplaySink();

/**
	 * Returns the meta object for class '{@link pipeline.ProcessingElement <em>Processing Element</em>}'.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Processing Element</em>'.
	 * @see pipeline.ProcessingElement
	 * @generated
	 */
  EClass getProcessingElement();

  /**
	 * Returns the meta object for class '{@link pipeline.FamilyElement <em>Family Element</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Family Element</em>'.
	 * @see pipeline.FamilyElement
	 * @generated
	 */
    EClass getFamilyElement();

/**
	 * Returns the meta object for the attribute '{@link pipeline.FamilyElement#getFamily <em>Family</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Family</em>'.
	 * @see pipeline.FamilyElement#getFamily()
	 * @see #getFamilyElement()
	 * @generated
	 */
    EAttribute getFamilyElement_Family();

/**
	 * Returns the meta object for the attribute '{@link pipeline.FamilyElement#getIsConnector <em>Is Connector</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Is Connector</em>'.
	 * @see pipeline.FamilyElement#getIsConnector()
	 * @see #getFamilyElement()
	 * @generated
	 */
	EAttribute getFamilyElement_IsConnector();

/**
	 * Returns the meta object for the attribute '{@link pipeline.FamilyElement#getDefaultAlgorithm <em>Default Algorithm</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Default Algorithm</em>'.
	 * @see pipeline.FamilyElement#getDefaultAlgorithm()
	 * @see #getFamilyElement()
	 * @generated
	 */
	EAttribute getFamilyElement_DefaultAlgorithm();

/**
	 * Returns the meta object for the attribute list '{@link pipeline.FamilyElement#getPermissibleParameters <em>Permissible Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Permissible Parameters</em>'.
	 * @see pipeline.FamilyElement#getPermissibleParameters()
	 * @see #getFamilyElement()
	 * @generated
	 */
	EAttribute getFamilyElement_PermissibleParameters();

/**
	 * Returns the meta object for class '{@link pipeline.DataManagementElement <em>Data Management Element</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Management Element</em>'.
	 * @see pipeline.DataManagementElement
	 * @generated
	 */
    EClass getDataManagementElement();

/**
	 * Returns the meta object for the attribute '{@link pipeline.DataManagementElement#getDataManagement <em>Data Management</em>}'.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Management</em>'.
	 * @see pipeline.DataManagementElement#getDataManagement()
	 * @see #getDataManagementElement()
	 * @generated
	 */
    EAttribute getDataManagementElement_DataManagement();

/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
  PipelineFactory getPipelineFactory();

  /**
	 * <!-- begin-user-doc -->
   * Defines literals for the meta objects that represent
   * <ul>
   *   <li>each class,</li>
   *   <li>each feature of each class,</li>
   *   <li>each enum,</li>
   *   <li>and each data type</li>
   * </ul>
   * <!-- end-user-doc -->
	 * @generated
	 */
  interface Literals
  {
    /**
		 * The meta object literal for the '{@link pipeline.impl.PipelineImpl <em>Pipeline</em>}' class.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @see pipeline.impl.PipelineImpl
		 * @see pipeline.impl.PipelinePackageImpl#getPipeline()
		 * @generated
		 */
    EClass PIPELINE = eINSTANCE.getPipeline();

    /**
		 * The meta object literal for the '<em><b>Nodes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @generated
		 */
    EReference PIPELINE__NODES = eINSTANCE.getPipeline_Nodes();

    /**
		 * The meta object literal for the '<em><b>Flows</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @generated
		 */
    EReference PIPELINE__FLOWS = eINSTANCE.getPipeline_Flows();

    /**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE__NAME = eINSTANCE.getPipeline_Name();

    /**
		 * The meta object literal for the '<em><b>Numworkers</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE__NUMWORKERS = eINSTANCE.getPipeline_Numworkers();

    /**
		 * The meta object literal for the '<em><b>Artifact</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE__ARTIFACT = eINSTANCE.getPipeline_Artifact();

				/**
		 * The meta object literal for the '<em><b>Constraints</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE__CONSTRAINTS = eINSTANCE.getPipeline_Constraints();

    /**
		 * The meta object literal for the '<em><b>Debug</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE__DEBUG = eINSTANCE.getPipeline_Debug();

				/**
		 * The meta object literal for the '<em><b>Fast Serialization</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE__FAST_SERIALIZATION = eINSTANCE.getPipeline_FastSerialization();

                /**
		 * The meta object literal for the '<em><b>Is Sub Pipeline</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE__IS_SUB_PIPELINE = eINSTANCE.getPipeline_IsSubPipeline();

																/**
		 * The meta object literal for the '<em><b>Sub Pipeline Family</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE__SUB_PIPELINE_FAMILY = eINSTANCE.getPipeline_SubPipelineFamily();

																/**
		 * The meta object literal for the '{@link pipeline.impl.PipelineElementImpl <em>Element</em>}' class.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @see pipeline.impl.PipelineElementImpl
		 * @see pipeline.impl.PipelinePackageImpl#getPipelineElement()
		 * @generated
		 */
        EClass PIPELINE_ELEMENT = eINSTANCE.getPipelineElement();

    /**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE_ELEMENT__NAME = eINSTANCE.getPipelineElement_Name();

    /**
		 * The meta object literal for the '<em><b>Constraints</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute PIPELINE_ELEMENT__CONSTRAINTS = eINSTANCE.getPipelineElement_Constraints();

    /**
		 * The meta object literal for the '{@link pipeline.impl.FlowImpl <em>Flow</em>}' class.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @see pipeline.impl.FlowImpl
		 * @see pipeline.impl.PipelinePackageImpl#getFlow()
		 * @generated
		 */
    EClass FLOW = eINSTANCE.getFlow();

    /**
		 * The meta object literal for the '<em><b>Source</b></em>' reference feature.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @generated
		 */
    EReference FLOW__SOURCE = eINSTANCE.getFlow_Source();

    /**
		 * The meta object literal for the '<em><b>Destination</b></em>' reference feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EReference FLOW__DESTINATION = eINSTANCE.getFlow_Destination();

    /**
		 * The meta object literal for the '<em><b>Grouping</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute FLOW__GROUPING = eINSTANCE.getFlow_Grouping();

    /**
		 * The meta object literal for the '<em><b>Tuple Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FLOW__TUPLE_TYPE = eINSTANCE.getFlow_TupleType();

				/**
		 * The meta object literal for the '{@link pipeline.impl.PipelineNodeImpl <em>Node</em>}' class.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @see pipeline.impl.PipelineNodeImpl
		 * @see pipeline.impl.PipelinePackageImpl#getPipelineNode()
		 * @generated
		 */
        EClass PIPELINE_NODE = eINSTANCE.getPipelineNode();

    /**
		 * The meta object literal for the '<em><b>Parallelism</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE_NODE__PARALLELISM = eINSTANCE.getPipelineNode_Parallelism();

				/**
		 * The meta object literal for the '<em><b>Numtasks</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PIPELINE_NODE__NUMTASKS = eINSTANCE.getPipelineNode_Numtasks();

				/**
		 * The meta object literal for the '{@link pipeline.impl.SourceImpl <em>Source</em>}' class.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @see pipeline.impl.SourceImpl
		 * @see pipeline.impl.PipelinePackageImpl#getSource()
		 * @generated
		 */
    EClass SOURCE = eINSTANCE.getSource();

    /**
		 * The meta object literal for the '<em><b>Source</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute SOURCE__SOURCE = eINSTANCE.getSource_Source();

    /**
		 * The meta object literal for the '<em><b>Permissible Parameters</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SOURCE__PERMISSIBLE_PARAMETERS = eINSTANCE.getSource_PermissibleParameters();

				/**
		 * The meta object literal for the '{@link pipeline.impl.SinkImpl <em>Sink</em>}' class.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @see pipeline.impl.SinkImpl
		 * @see pipeline.impl.PipelinePackageImpl#getSink()
		 * @generated
		 */
    EClass SINK = eINSTANCE.getSink();

    /**
		 * The meta object literal for the '<em><b>Sink</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute SINK__SINK = eINSTANCE.getSink_Sink();

    /**
		 * The meta object literal for the '<em><b>Permissible Parameters</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SINK__PERMISSIBLE_PARAMETERS = eINSTANCE.getSink_PermissibleParameters();

				/**
		 * The meta object literal for the '{@link pipeline.impl.ReplaySinkImpl <em>Replay Sink</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see pipeline.impl.ReplaySinkImpl
		 * @see pipeline.impl.PipelinePackageImpl#getReplaySink()
		 * @generated
		 */
		EClass REPLAY_SINK = eINSTANCE.getReplaySink();

				/**
		 * The meta object literal for the '{@link pipeline.impl.ProcessingElementImpl <em>Processing Element</em>}' class.
		 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
		 * @see pipeline.impl.ProcessingElementImpl
		 * @see pipeline.impl.PipelinePackageImpl#getProcessingElement()
		 * @generated
		 */
    EClass PROCESSING_ELEMENT = eINSTANCE.getProcessingElement();

    /**
		 * The meta object literal for the '{@link pipeline.impl.FamilyElementImpl <em>Family Element</em>}' class.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @see pipeline.impl.FamilyElementImpl
		 * @see pipeline.impl.PipelinePackageImpl#getFamilyElement()
		 * @generated
		 */
        EClass FAMILY_ELEMENT = eINSTANCE.getFamilyElement();

    /**
		 * The meta object literal for the '<em><b>Family</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute FAMILY_ELEMENT__FAMILY = eINSTANCE.getFamilyElement_Family();

    /**
		 * The meta object literal for the '<em><b>Is Connector</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FAMILY_ELEMENT__IS_CONNECTOR = eINSTANCE.getFamilyElement_IsConnector();

				/**
		 * The meta object literal for the '<em><b>Default Algorithm</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FAMILY_ELEMENT__DEFAULT_ALGORITHM = eINSTANCE.getFamilyElement_DefaultAlgorithm();

				/**
		 * The meta object literal for the '<em><b>Permissible Parameters</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FAMILY_ELEMENT__PERMISSIBLE_PARAMETERS = eINSTANCE.getFamilyElement_PermissibleParameters();

				/**
		 * The meta object literal for the '{@link pipeline.impl.DataManagementElementImpl <em>Data Management Element</em>}' class.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @see pipeline.impl.DataManagementElementImpl
		 * @see pipeline.impl.PipelinePackageImpl#getDataManagementElement()
		 * @generated
		 */
        EClass DATA_MANAGEMENT_ELEMENT = eINSTANCE.getDataManagementElement();

    /**
		 * The meta object literal for the '<em><b>Data Management</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
         * <!-- end-user-doc -->
		 * @generated
		 */
        EAttribute DATA_MANAGEMENT_ELEMENT__DATA_MANAGEMENT = eINSTANCE.getDataManagementElement_DataManagement();

  }

} //PipelinePackage
