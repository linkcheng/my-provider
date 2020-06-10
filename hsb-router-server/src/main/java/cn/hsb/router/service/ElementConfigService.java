package cn.hsb.router.service;

import cn.hsb.router.entity.ElementConfig;

import java.util.List;
import java.util.Set;

public interface ElementConfigService {
    List<ElementConfig> getAll();
    List<ElementConfig> getMany(List<String> names);
    ElementConfig getOne(String name);

    void insertOne(ElementConfig elementConfig);
    void insertMany(List<ElementConfig> elementConfigs);

    void updateOne(ElementConfig elementConfig);
    void updateMany(List<ElementConfig> elementConfigs);
    void deleteOne(String name);
    void deleteMany(Set<String> names);
}
