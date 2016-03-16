package de.uni_hildesheim.sse.qmApp.dialogs;

import de.uni_hildesheim.sse.easy.ui.productline_editor.EclipseConsole;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.IBuildlangElement;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.IEnumeratingLoop;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.Rule;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.Script;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.RuntimeEnvironment;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VariableDeclaration;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.common.VilException;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.execution.IInstantiatorTracer;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.execution.TracerFactory;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.expressions.CallExpression.CallType;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.expressions.Expression;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.Def;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.ITemplateLangElement;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.ITracer;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.Template;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.Collection;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.FieldDescriptor;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.OperationDescriptor;

/**
 * Implements a simplified tracer factory for the QM configuration application.
 * 
 * @author Holger Eichelberger
 */
public class UiTracerFactory extends TracerFactory {
    
    /**
     * The tracer factory instance. 
     */
    public static final TracerFactory INSTANCE = new UiTracerFactory();

    private static final Tracer TRACER = new Tracer();

    /**
     * Constructs a new tracer factory and sets this instance
     * as the default tracer.
     */
    private UiTracerFactory() {
        super.setInstance(this);
    }
    
    @Override
    protected ITracer createTemplateLanguageTracerImpl() {
        return TRACER;
    }

    @Override
    protected de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.ITracer
        createBuildLanguageTracerImpl() {
        return TRACER;
    }
    
    @Override
    protected IInstantiatorTracer createInstantiatorTracerImpl() {
        return TRACER;
    }
    
    /**
     * Writes a message to the default EASy Eclipse console.
     * 
     * @param msg the message to be written
     */
    private static void write(String msg) {
        EclipseConsole.INSTANCE.writeToConsole(msg, true);
    }
    
    /**
     * Implements the simplifying tracer for the QM app.
     * 
     * @author Holger Eichelberger
     */
    private static class Tracer implements 
        de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.ITracer, 
        de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.ITracer, IInstantiatorTracer {

        @Override
        public void trace(String text) {
        }

        @Override
        public void valueDefined(VariableDeclaration var, FieldDescriptor field, Object value) {
        }

        @Override
        public void traceExecutionException(VilException exception) {
            write("ERROR: " + exception.getMessage());
        }

        @Override
        public void visitingCallExpression(OperationDescriptor descriptor, CallType callType, Object[] args) {
        }

        @Override
        public void visitedCallExpression(OperationDescriptor descriptor, CallType callType, Object[] args,
            Object result) {
        }

        @Override
        public void failedAt(Expression expression) {
        }

        @Override
        public void visitTemplate(Template template) {
            write("Executing script: " + template.getName());
        }

        @Override
        public void visitedTemplate(Template template) {
        }

        @Override
        public void visitDef(Def def, RuntimeEnvironment environment) {
        }

        @Override
        public void visitedDef(Def def, RuntimeEnvironment environment, Object result) {
        }

        @Override
        public void visitedSwitch(Object select, int alternative, Object value) {
        }

        @Override
        public void visitLoop(VariableDeclaration var) {
        }

        @Override
        public void visitedLoop(VariableDeclaration var) {
        }

        @Override
        public void visitAlternative(boolean takeIf) {
        }

        @Override
        public void failedAt(ITemplateLangElement element) {
        }

        @Override
        public void visitScript(Script script) {
            write("Executing script: " + script.getName());
        }

        @Override
        public void visitedScript(Script script) {
        }

        @Override
        public void visitRule(Rule rule, RuntimeEnvironment environment) {
        }

        @Override
        public void visitedRule(Rule rule, RuntimeEnvironment environment, Object result) {
        }

        @Override
        public Collection<?> adjustSequenceForMap(Collection<?> collection) {
            return collection;
        }

        @Override
        public Collection<Object> adjustSequenceForJoin(Collection<Object> collection) {
            return collection;
        }

        @Override
        public void visitSystemCall(String[] args) {
        }

        @Override
        public void visitingInstantiator(String name) {
        }

        @Override
        public void visitedInstantiator(String name, Object result) {
        }

        @Override
        public void failedAt(IBuildlangElement element) {
        }
       
        @Override
        public void reset() {
        }

        @Override
        public void traceMessage(String message) {
            write(message);
        }

        @Override
        public void traceError(String message) {
            write(message);
        }

        @Override
        public void visitLoop(IEnumeratingLoop loop, RuntimeEnvironment environment) {
        }

        @Override
        public void visitIteratorAssignment(IEnumeratingLoop loop,
            de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.VariableDeclaration var,
            Object value) {
        }

        @Override
        public void visitedLoop(IEnumeratingLoop loop, RuntimeEnvironment environment) {
        }

        @Override
        public void visitWhileBody() {
        }

        @Override
        public void visitedWhileBody() {
        }

    }
    
}
