package eu.qualimaster.manifestUtils;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import net.ssehub.easy.basics.progress.ProgressObserver;

/**
 * Intercepts ivy console output during the publishing process.
 * @author pastuschek
 *
 */
public class UploadIntercepter extends AbstractIntercepter {

    /**
     * Simple constructor, since the super constructor is sufficient.
     * @param output The output for this intercepter.
     * @param original The original PrintStream.
     * @throws FileNotFoundException if the intercepter could not be initialized.
     */
    public UploadIntercepter(String output, PrintStream original) throws FileNotFoundException {
        super(output, original);
    }

    @Override
    public void process(String string) {
        if (null != string) {
            if (string.contains("found") || string.contains("downloading")) {
                int max = this.getMax();
                ProgressObserver monitor = this.getMonitor();
                if (this.getCurrentProgress() < max) {
                    monitor.notifyProgress(this.getMainTask(), this.getCurrentProgress());
                    ProgressObserver.ISubtask subtask = monitor.registerSubtask(string);
                    monitor.notifyStart(this.getMainTask(), subtask, 1);
                    this.setCurrentProgress(this.getCurrentProgress() + 1);
                } else {
                    monitor.notifyProgress(this.getMainTask(), this.getCurrentProgress(), max + 1);
                }
            }
        }
    }

}
