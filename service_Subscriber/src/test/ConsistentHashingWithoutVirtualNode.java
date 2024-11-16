package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsistentHashingWithoutVirtualNode {
	private static final int NUM_THREADS = 4; // 线程池大小
	private static final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
}
