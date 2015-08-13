package nx.engine.data;

public class DataEnginePool {
	IDataEngine[] dataEnginePool;
	int poolSize;
	int currentDataEngineIndex = 0;

	public DataEnginePool(int poolSize, IDataEngine dataEngine)
			throws Exception {
		this.poolSize = poolSize;
		dataEnginePool = new IDataEngine[poolSize];
		dataEnginePool[0] = dataEngine;
		for (int i = 1; i < poolSize; i++) {
			dataEnginePool[i] = dataEngine.newSession();
		}
	}

	public IDataEngine getDataEngine() {
		if (currentDataEngineIndex == poolSize) {
			currentDataEngineIndex = 0;
		}
		IDataEngine cache = dataEnginePool[currentDataEngineIndex];
		currentDataEngineIndex++;
		return cache;
	}

	public IDataEngine getDefaultDataEngine() {
		return dataEnginePool[0];
	}

	public void close() throws Exception {
		for (IDataEngine engine : dataEnginePool) {
			engine.close();
		}
	}
}
