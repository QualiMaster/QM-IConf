package eu.qualimaster.manifestUtils;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.basics.progress.ProgressObserver.ITask;

/**
 * This class is used to intercept any output of the ivy component.
 * @author pastuschek
 *
 */
public abstract class AbstractIntercepter extends PrintStream {
    
    private String output;
    private ProgressObserver monitor = null;
    private ITask mainTask = null;
    private int prog = 1;
    private int max = 1;
    private PrintStream original;
    
    /**
     * Simple constructor.
     * @param output the output String.
     * @param original the original PrintStream.
     * @throws FileNotFoundException 
     */
    public AbstractIntercepter(String output, PrintStream original) throws FileNotFoundException {
        super(System.out, true);
        this.output = output;     
        this.original = original;
    }
    
    /**
     * Returns whether output should be processed or just stacked.
     * @return True if output should be processed, false otherwise.
     */
    public boolean isProcessing() {
        return (null != monitor && null != mainTask);
    }
    
    /**
     * The String to process.
     * @param string The String to process.
     */
    public abstract void process(String string);
    
    /**
     * Redirect the intercepted output.
     */
    @Override
    public void print(String string) {
        this.output += (string);
        this.original.print(string);
        if (isProcessing()) {
            process(string);
        }
    }
    
    /**
     * Redirect the intercepted output.
     */
    @Override
    public void println(String string) {
        this.output += ("\n" + string);
        this.original.println(string);
        if (isProcessing()) {
            process(string);
        }
    }
    
    /**
     * Sets the maximum progess for the monitor.
     * @param max The maximum for the monitor.
     */
    public void setMax(int max) {
        this.max = max;
    }
    
    /**
     * Returns the maximum progess for the monitor.
     * @return max The maximum for the monitor.
     */
    protected int getMax() {
        return this.max;
    }
    
    /**
     * Sets the monitor that will be updated by intercepted information.
     * @param monitor The monitor.
     */
    public void setMonitor(ProgressObserver monitor) {
        this.monitor = monitor;
    }
    
    /**
     * Returns the monitor that will be updated by intercepted information.
     * @return monitor The ProgressObserver.
     */
    protected ProgressObserver getMonitor() {
        return this.monitor;
    }
    
    /**
     * Sets the main task. Can be null.
     * @param task The current task, can be null.
     */
    public void setTask(ITask task) {
        this.mainTask = task;
        this.prog = 1;
    }
    
    /**
     * Returns the main task of this intercepter.
     * @return The mainTask as ITask.
     */
    protected ITask getMainTask() {
        return this.mainTask;
    }
    
    /**
     * Returns the current progress as int. Note that this is not a percentage value,
     * but a total value instead and has to be counter checked via the maximum.
     * @return prog The current progress value as int.
     */
    protected int getCurrentProgress() {
        return this.prog;
    }
    
    /**
     * Sets the current progress to a given value.
     * @param prog The new progress value as int.
     */
    protected void setCurrentProgress(int prog) {
        this.prog = prog;
    }
    
    /**
     * Returns the intercepted output.
     * @return The intercepted output String.
     */
    public String getOutput() {
        String buffer = this.output;
        this.output = "";
        return buffer;
    }
    
}
