package com.drools.test.com.drools.test.kie.file.system;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DroolsResultProcessor {

	private Queue<Object> captureEvents = new ConcurrentLinkedQueue<Object>();

	public void processResults(Object captureEvent) {
		captureEvents.add(captureEvent);
	}

	public Object getCaptureEvent() {
		return captureEvents.poll();
	}
}
