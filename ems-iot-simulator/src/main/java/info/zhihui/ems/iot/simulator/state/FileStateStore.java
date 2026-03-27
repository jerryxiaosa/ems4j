package info.zhihui.ems.iot.simulator.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.zhihui.ems.common.utils.JacksonUtil;
import info.zhihui.ems.iot.simulator.config.SimulatorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 基于本地文件的状态存储。
 */
@Service
public class FileStateStore implements StateStore {

    private final Path stateFilePath;
    private final ObjectMapper objectMapper;

    @Autowired
    public FileStateStore(SimulatorProperties simulatorProperties, ObjectMapper objectMapper) {
        this(simulatorProperties.getRuntime().getPersistenceFile(), objectMapper);
    }

    public FileStateStore(String persistenceFile) {
        this(persistenceFile, JacksonUtil.getObjectMapper());
    }

    public FileStateStore(String persistenceFile, ObjectMapper objectMapper) {
        this.stateFilePath = Path.of(persistenceFile);
        this.objectMapper = objectMapper;
    }

    @Override
    public SimulatorStateSnapshot load() {
        if (!Files.exists(stateFilePath)) {
            return new SimulatorStateSnapshot();
        }
        try {
            return objectMapper.readValue(stateFilePath.toFile(), SimulatorStateSnapshot.class);
        } catch (IOException ex) {
            throw new IllegalStateException("读取模拟器状态文件失败", ex);
        }
    }

    @Override
    public void save(SimulatorStateSnapshot snapshot) {
        SimulatorStateSnapshot stateSnapshot = snapshot == null ? new SimulatorStateSnapshot() : snapshot;
        try {
            Path parentPath = stateFilePath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }
            Path tempFilePath = buildTempFilePath();
            objectMapper.writeValue(tempFilePath.toFile(), stateSnapshot);
            moveAtomically(tempFilePath);
        } catch (IOException ex) {
            throw new IllegalStateException("保存模拟器状态文件失败", ex);
        }
    }

    private Path buildTempFilePath() {
        String fileName = stateFilePath.getFileName().toString();
        return stateFilePath.resolveSibling(fileName + ".tmp");
    }

    private void moveAtomically(Path tempFilePath) throws IOException {
        try {
            Files.move(tempFilePath, stateFilePath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(tempFilePath, stateFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
