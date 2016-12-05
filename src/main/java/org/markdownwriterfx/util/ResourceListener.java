package org.markdownwriterfx.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.markdownwriterfx.util.ResourceListener.IResourceUpdatedHandle;

public class ResourceListener {
	public static interface IResourceUpdatedHandle {
		public void updatedHandle();
	}

	private static ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
	private WatchService ws;
	private String listenerPath;
	private IResourceUpdatedHandle handle;

	public static void main(String[] args) throws IOException {
		ResourceListener.addListener("E:/aaa", new IResourceUpdatedHandle() {

			@Override
			public void updatedHandle() {

			}
		});
	}

	private ResourceListener(String path, IResourceUpdatedHandle handle) {
		try {
			ws = FileSystems.getDefault().newWatchService();
			this.listenerPath = path;
			this.handle = handle;
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void start() {
		fixedThreadPool.execute(new Listner(ws, this.listenerPath, this.handle));
	}

	public static void addListener(String path, IResourceUpdatedHandle handle) {
		try {
			ResourceListener resourceListener = new ResourceListener(path, handle);
			Path p = Paths.get(path);
			p.register(resourceListener.ws, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_CREATE);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}

class Listner implements Runnable {
	private final WatchService service;
	private final String rootPath;
	private final IResourceUpdatedHandle handle;

	public Listner(WatchService service, String rootPath, IResourceUpdatedHandle handle) {
		this.service = service;
		this.rootPath = rootPath;
		this.handle = handle;
	}

	@Override
	public void run() {
		try {
			while (true) {
				WatchKey watchKey = service.take();
				List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
				for (WatchEvent<?> event : watchEvents) {
					System.out.println("[" + rootPath + "/" + event.context() + "]文件发生了[" + event.kind() + "]事件");
					handle.updatedHandle();
				}
				watchKey.reset();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Some exceptions happened.Resource listener exited.");
			try {
				service.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
