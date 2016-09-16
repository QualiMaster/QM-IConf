package eu.qualimaster.manifestUtils;

import java.io.File;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.basics.progress.ProgressObserver.ITask;

/**
 * Implementation of the copyStreamListener interface to keep track of upload progress.
 * @author pastuschek
 *
 */
public class UploadListener implements CopyStreamListener {

    private ProgressObserver monitor;
    private File source;
    private ITask task;
    private boolean done = false;
    private int mbTransferred = 0;
    private int mbSize = 0;
    
    /**
     * Alternative constructor for the use with a ProgressObserver.
     * @param monitor The ProgressObserver to inform of progress.
     * @param source The file that is being observed by this listener-observer combination.
     */
    public UploadListener(ProgressObserver monitor, File source) {
        
        //initialize
        super();
        this.monitor = monitor;
        this.source = source;
        this.task = monitor.registerTask(source.getName());
        monitor.notifyStart(this.task, 100);
        this.done = false;
        
        System.out.println("Starting upload task for: " + source.getAbsolutePath());
        
        mbSize = (int) ((double) source.length() / 1000000);
        ProgressObserver.ISubtask subtask = monitor.registerSubtask(
                0 + " / " + mbSize + " Mbyte");
        monitor.notifyStart(this.task, subtask, 1);
        
    }
    
    @Override
    public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(),
            event.getStreamSize());
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        
        //calculate the current progress and the according percentage.
        int transfer = (int) ((double) totalBytesTransferred / 1000000);
        double progress = ((double) totalBytesTransferred / source.length());
        int step = ((int) (progress * 100));
        
        //if 100% is reached, end the task.
        if (step >= 100) {
            
            monitor.notifyProgress(this.task, 100);
            monitor.notifyEnd(this.task);
            System.out.println("Finished upload task for: " + source.getAbsolutePath());
            done = true;
            
        } else {
            
            //if the task is not done keep updating the progress.
            if (!done) {
                
                monitor.notifyProgress(this.task, step);
                
                //update only if we have actually made some progress.
                if (transfer > mbTransferred) {
                    
                    mbTransferred = transfer;
                    ProgressObserver.ISubtask subtask = monitor.registerSubtask(
                            mbTransferred + " / " + mbSize + " Mbyte");
                    monitor.notifyStart(this.task, subtask, 1);
                    
                }
                
            } 
            
        }
        
    }
    
    /**
     * Returns the ProgressObserver of this listener.
     * @return A ProgressObserver, can be null.
     */
    public ProgressObserver getMonitor() {
        return this.monitor;
    }
    
    /**
     * Ends the current task.
     */
    public void endTask() {
        try {     
            monitor.notifyEnd(this.task);
            done = true;
        } catch (IllegalArgumentException exc) {
            //ignore this exception as the task is obviously done or not started!
        }
    }
    
}
