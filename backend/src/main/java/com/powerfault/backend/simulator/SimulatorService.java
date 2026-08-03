//
//
//package com.powerfault.backend.simulator;
//
//import com.powerfault.backend.simulator.dto.SimulatorRequest;
//
//public interface SimulatorService {
//
//    void simulateSpanFault(String poleCode);
//
//    void simulateTransformerFault(String transformerCode);
//
//    void simulateFeederFault(String feederCode);
//
//    void simulateDeviceFailure(String deviceId);
//
//    void repairFault(String poleCode);
//
//    void simulate(SimulatorRequest request);
//
//
//}

package com.powerfault.backend.simulator;

public interface SimulatorService {

    void simulateSpanFault(String poleCode);

    void simulateTransformerFault(String transformerCode);

    void simulateFeederFault(String feederCode);

    void simulateDeviceFailure(String deviceId);

    void repairFault(String poleCode);
}