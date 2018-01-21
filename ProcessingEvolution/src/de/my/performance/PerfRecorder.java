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

	static private PerfRecorder singleton = new PerfRecorder();

	public static PerfRecorder instance() {
		return singleton;
	}

	public PerfRecorder() {
		final SimpleDateFormat form = new SimpleDateFormat("yyMMdd-HHmmss");
		final Date now = new Date();
		init("It took: ", "performance\\perf_" + form.format(now));
	}

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
	 * Record the measure into the performance file using the current label.
	 * Ensures stop, but will only record, if we are measuring.
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
