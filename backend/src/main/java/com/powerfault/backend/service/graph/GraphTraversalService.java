package com.powerfault.backend.service.graph;

import com.powerfault.backend.entity.Pole;

import java.util.List;

public interface GraphTraversalService {

    List<Pole> getDownstreamPoles(Pole startPole);

}