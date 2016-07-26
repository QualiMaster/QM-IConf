package eu.qualimaster.manifestUtils;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import net.ssehub.easy.basics.progress.ProgressObserver;

/**
 * Intercepts ivys console output for download tasks.
 * @author pastuschek
 *
 */
public class DownloadIntercepter extends AbstractIntercepter {

    /**
     * Simple constructor, since the provided constructor is sufficient.
     * @param output the output String.
     * @param original the original PrintStream.
     * @throws FileNotFoundException if the intercepter could not be initialized.
     */
    public DownloadIntercepter(String output, PrintStream original) throws FileNotFoundException {
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
