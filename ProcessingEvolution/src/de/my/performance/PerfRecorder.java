package de.my.performance;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.my.performance.PerfRecorder.InternalState.*;

/**
 * Stop-Watch like measure. It may keep one current time.
 * 
 * @author Oliver Meyer
 *
 */
public class PerfRecorder {
    protected enum InternalState {
        Running, // We are currently doing a measure (started, not yet stopped)
        NotStarted, // Not yet started, no pending write
        Stopped, // We have been stopped and can eventually write the timing.
    }

    private PrintStream perfLog;
    private long endTime;
    private long startTime;
    private InternalState state = NotStarted;
    private String label;

    static private PerfRecorder singleton = new PerfRecorder("Singleton");

    /**
     * Return precreated singleton instance.
     * @return the one special perfrecorder
     */
    public static PerfRecorder instance() {
        return singleton;
    }

    /**
     * Create new Performance recorder with default label and filename. This is equivalent to calling
     * {@link #PerfRecorder(String) PerfRecorder("perf")}.
     */
    public PerfRecorder() {
        this("perf");
    }

    /**
     * Create new Performance recorder with default label and customized filename. The file used follows the same
     * conventions as the default filename used with {@link #PerfRecorder()}, but also has a specific prefix.
     * 
     * @param filePrefix
     */
    public PerfRecorder(String filePrefix) {
        final SimpleDateFormat form = new SimpleDateFormat("yyMMdd-HHmmssSSS");
        final Date now = new Date();
        init("It took: ", "performance\\" + filePrefix + "_" + form.format(now));

    }

    /**
     * Create a new Performance Recorder with the given label and file. The label can be changed later through
     * {@link #setLabel(String)}.
     * 
     * @param label
     *            each entry is prefixed with this label
     * @param file
     *            Absolute filename to use for recording
     */
    public PerfRecorder(String label, String file) {
        init(label, file);
    }

    /**
     * Updates the label to be used in the next recording.
     * 
     * @param label
     *            The label to use for all subsequent recordings.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Start a new timing. Will restart the timing if already running.
     */
    public void startTiming() {
        beginMeasure();
    }

    /**
     * Stops, and records the measure.
     * 
     * Record the measure into the performance file using the current label. Ensures stop, but will only record, if we
     * are measuring.
     */
    public void stopTiming() {
        switch (state) {
        case NotStarted:
        case Stopped:
            break;
        case Running:
            endMeasure();
            writeTiming();
        }
        state = NotStarted;
    }

    /**
     * Stops, records, and starts the timing.
     */
    public void recordIteration() {
        stopTiming();
        startTiming();
    }

    /**
     * Record the current timing, but continue measuring.
     */
    public void dumpTiming() {
        switch (state) {
        case NotStarted:
            break;
        case Stopped:
        case Running:
            writeTiming();
            break;
        }
    }

    /**
     * Writes the current label and measure to the log. Stateless.
     */
    private void writeTiming() {
        perfLog.println(label + (endTime - startTime));
        perfLog.flush();
    }

    private void beginMeasure() {
        startTime = System.currentTimeMillis();
        state = Running;
    }

    private void endMeasure() {
        endTime = System.currentTimeMillis();
        state = Stopped;
    }

    protected void init(String label, String filename) {
        this.label = label;
        try {
            perfLog = new PrintStream(filename, "ASCII");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
