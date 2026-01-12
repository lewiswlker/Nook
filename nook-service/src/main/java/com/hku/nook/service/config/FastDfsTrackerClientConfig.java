package com.hku.nook.service.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.tobato.fastdfs.domain.fdfs.GroupState;
import com.github.tobato.fastdfs.domain.fdfs.StorageNode;
import com.github.tobato.fastdfs.domain.fdfs.StorageNodeInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorageState;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import com.github.tobato.fastdfs.service.TrackerClient;

@Configuration
public class FastDfsTrackerClientConfig {

    private static final String DOCKER_SUBNET_PREFIX = "172.20.";
    private static final String LOCALHOST = "127.0.0.1";

    @Value("${fastdfs.storage.use-localhost:false}")
    private boolean useLocalhost;

    @Bean
    @Primary
    public TrackerClient trackerClient(DefaultTrackerClient delegate) {
        return new TrackerClient() {
            @Override
            public StorageNode getStoreStorage() {
                return mapNode(delegate.getStoreStorage());
            }

            @Override
            public StorageNode getStoreStorage(String groupName) {
                return mapNode(delegate.getStoreStorage(groupName));
            }

            @Override
            public StorageNodeInfo getFetchStorage(String groupName, String path) {
                return mapNodeInfo(delegate.getFetchStorage(groupName, path));
            }

            @Override
            public StorageNodeInfo getUpdateStorage(String groupName, String path) {
                return mapNodeInfo(delegate.getUpdateStorage(groupName, path));
            }

            @Override
            public List<GroupState> listGroups() {
                return delegate.listGroups();
            }

            @Override
            public List<StorageState> listStorages(String groupName) {
                return mapStorageStates(delegate.listStorages(groupName));
            }

            @Override
            public List<StorageState> listStorages(String groupName, String storageIpAddr) {
                return mapStorageStates(delegate.listStorages(groupName, storageIpAddr));
            }

            @Override
            public void deleteStorage(String groupName, String storageIpAddr) {
                delegate.deleteStorage(groupName, storageIpAddr);
            }
        };
    }

    private StorageNode mapNode(StorageNode node) {
        if (node == null || !shouldMapIp(node.getIp())) {
            return node;
        }
        StorageNode mapped = new StorageNode();
        mapped.setGroupName(node.getGroupName());
        mapped.setIp(LOCALHOST);
        mapped.setPort(node.getPort());
        mapped.setStoreIndex(node.getStoreIndex());
        return mapped;
    }

    private StorageNodeInfo mapNodeInfo(StorageNodeInfo node) {
        if (node == null || !shouldMapIp(node.getIp())) {
            return node;
        }
        StorageNodeInfo mapped = new StorageNodeInfo();
        mapped.setGroupName(node.getGroupName());
        mapped.setIp(LOCALHOST);
        mapped.setPort(node.getPort());
        return mapped;
    }

    private List<StorageState> mapStorageStates(List<StorageState> states) {
        if (states == null || states.isEmpty() || !useLocalhost) {
            return states;
        }
        for (StorageState state : states) {
            if (state != null && shouldMapIp(state.getIpAddr())) {
                state.setIpAddr(LOCALHOST);
            }
        }
        return states;
    }

    private boolean shouldMapIp(String ip) {
        return useLocalhost && ip != null && ip.startsWith(DOCKER_SUBNET_PREFIX);
    }
}
