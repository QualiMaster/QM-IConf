package de.uni_hildesheim.sse.qmApp.dialogs;

import net.ssehub.easy.instantiation.core.model.buildlangModel.IBuildlangElement;
import net.ssehub.easy.instantiation.core.model.buildlangModel.IEnumeratingLoop;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Rule;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.common.ITraceFilter;
import net.ssehub.easy.instantiation.core.model.common.RuntimeEnvironment;
import net.ssehub.easy.instantiation.core.model.common.VariableDeclaration;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.execution.IInstantiatorTracer;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.expressions.Expression;
import net.ssehub.easy.instantiation.core.model.expressions.AbstractTracerBase;
import net.ssehub.easy.instantiation.core.model.expressions.CallExpression.CallType;
import net.ssehub.easy.instantiation.core.model.templateModel.Def;
import net.ssehub.easy.instantiation.core.model.templateModel.ITemplateLangElement;
import net.ssehub.easy.instantiation.core.model.templateModel.ITracer;
import net.ssehub.easy.instantiation.core.model.templateModel.Template;
import net.ssehub.easy.instantiation.core.model.vilTypes.Collection;
import net.ssehub.easy.instantiation.core.model.vilTypes.FieldDescriptor;
import net.ssehub.easy.instantiation.core.model.vilTypes.OperationDescriptor;
import net.ssehub.easy.producer.ui.productline_editor.EclipseConsole;

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
        setDefaultInstance(this);
    }
    
    @Override
    public ITracer createTemplateLanguageTracerImpl() {
        return TRACER;
    }

    @Override
    public net.ssehub.easy.instantiation.core.model.buildlangModel.ITracer
        createBuildLanguageTracerImpl() {
        return TRACER;
    }
    
    @Override
    public IInstantiatorTracer createInstantiatorTracerImpl() {
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
    private static class Tracer extends AbstractTracerBase implements 
        net.ssehub.easy.instantiation.core.model.buildlangModel.ITracer, 
        net.ssehub.easy.instantiation.core.model.templateModel.ITracer, IInstantiatorTracer {

        @Override
        public void setTraceFilter(ITraceFilter filter) {
        }

        @Override
        public ITraceFilter getTraceFilter() {
            return null;
        }

        @Override
        public void trace(String text) {
        }

        @Override
        public void traceWarning(String text) {
            write("WARNING: " + text);
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
        public void visitDef(Def def, RuntimeEnvironment<?, ?> environment) {
        }

        @Override
        public void visitedDef(Def def, RuntimeEnvironment<?, ?> environment, Object result) {
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
        public void visitScript(Script script, RuntimeEnvironment<?, ?> environment) {
            write("Executing script: " + script.getName());
        }

        @Override
        public void visitedScript(Script script) {
        }

        @Override
        public void visitRule(Rule rule, RuntimeEnvironment<?, ?> environment) {
        }

        @Override
        public void visitedRule(Rule rule, RuntimeEnvironment<?, ?> environment, Object result) {
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
        public void visitLoop(IEnumeratingLoop loop, RuntimeEnvironment<?, ?> environment) {
        }

        @Override
        public void visitIteratorAssignment(IEnumeratingLoop loop,
            net.ssehub.easy.instantiation.core.model.buildlangModel.VariableDeclaration var,
            Object value) {
        }

        @Override
        public void visitedLoop(IEnumeratingLoop loop, RuntimeEnvironment<?, ?> environment) {
        }

        @Override
        public void visitWhileBody() {
        }

        @Override
        public void visitedWhileBody() {
        }

        @Override
        public void visitScriptBody(Script script, RuntimeEnvironment<?, ?> environment) {
        }

        @Override
        public void enable(boolean enable) {
            // ignore, is anyway a reduced tracer
        }

        @Override
        public void visitFlush() {
        }

        @Override
        public void visitedFlush() {
        }

    }
    
}
