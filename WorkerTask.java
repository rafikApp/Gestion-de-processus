package server;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class WorkerTask implements Runnable {

	private Command command;
	private ObjectOutputStream outputStream;
	private Worker worker;

	public WorkerTask(Worker worker, Command command, ObjectOutputStream outputStream) {
		this.command = command;
		this.worker = worker;
		this.outputStream = outputStream;
	}

	@Override
	public void run() {
		String param = command.getParameter();
		String result = null;
		if (param.contains(":")) {
			int from = Integer.valueOf(param.split(":")[0]);
			int to = Integer.valueOf(param.split(":")[1]);
			result = String.valueOf(MathUtils.computePersistance(from, to));
		} else {
			result = String.valueOf(MathUtils.computePersistance(Integer.valueOf(param)));
		}
		command.setResult(result);
		command.setExecuted();
		try {
			// pour debuger
			// Thread.sleep(50000);
			outputStream.writeObject(command);
			worker.setTaskDone();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
