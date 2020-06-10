package cn.hsb.router.service.impl;

import cn.hsb.router.mapper.birouter.ElementConfigMapper;
import cn.hsb.router.entity.ElementConfig;
import cn.hsb.router.service.ElementConfigService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ElementConfigServiceImpl implements ElementConfigService {
    @Autowired
    private ElementConfigMapper elementConfigMapper;

    @Override
    public List<ElementConfig> getAll() {
        return elementConfigMapper.getAll();
    }

    @Override
    public List<ElementConfig> getMany(List<String> names) {
        if (names == null || names.isEmpty()) {
            return Lists.newArrayList();
        }
        return elementConfigMapper.getMany(names);
    }

    @Override
    public ElementConfig getOne(String name) {
        return elementConfigMapper.getOne(name);
    }

    @Override
    public void insertOne(ElementConfig elementConfig) {
        elementConfigMapper.insertOne(elementConfig);
    }

    @Override
    public void insertMany(List<ElementConfig> elementConfigs) {
        if (elementConfigs == null || elementConfigs.isEmpty()) {
            return;
        }
        elementConfigMapper.insertMany(elementConfigs);
    }

    @Override
    public void updateOne(ElementConfig elementConfig) {
        elementConfigMapper.updateOne(elementConfig);
    }

    @Override
    public void updateMany(List<ElementConfig> elementConfigs) {
        if (elementConfigs == null || elementConfigs.isEmpty()) {
            return;
        }
        elementConfigMapper.updateMany(elementConfigs);
    }

    @Override
    public void deleteOne(String name) {
        elementConfigMapper.deleteOne(name);
    }

    @Override
    public void deleteMany(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return;
        }
        elementConfigMapper.deleteMany(names);
    }
}
