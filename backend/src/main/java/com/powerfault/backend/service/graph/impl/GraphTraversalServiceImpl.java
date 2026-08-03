package com.powerfault.backend.service.graph.impl;

import com.powerfault.backend.entity.NetworkConnection;
import com.powerfault.backend.entity.Pole;
import com.powerfault.backend.repository.NetworkConnectionRepository;
import com.powerfault.backend.repository.PoleRepository;
import com.powerfault.backend.service.graph.GraphTraversalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GraphTraversalServiceImpl
        implements GraphTraversalService {

    private final NetworkConnectionRepository connectionRepository;

    private final PoleRepository poleRepository;

    @Override
    public List<Pole> getDownstreamPoles(Pole startPole) {

        List<Pole> result = new ArrayList<>();

        Set<Long> visited = new HashSet<>();

        dfs(startPole, result, visited);

        return result;
    }

    private void dfs(
            Pole pole,
            List<Pole> result,
            Set<Long> visited) {

        if (visited.contains(pole.getId())) {
            return;
        }

        visited.add(pole.getId());

        result.add(pole);

        List<NetworkConnection> children =
                connectionRepository.findByFromPoleId(
                        pole.getId());

        for (NetworkConnection edge : children) {

            Pole child = edge.getToPole();

            dfs(child, result, visited);

        }

    }



}