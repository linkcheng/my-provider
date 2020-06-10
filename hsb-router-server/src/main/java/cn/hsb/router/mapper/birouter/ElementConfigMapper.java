package cn.hsb.router.mapper.birouter;

import cn.hsb.router.config.BiRouterMapper;
import cn.hsb.router.entity.ElementConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@BiRouterMapper
@Repository
public interface ElementConfigMapper {
    List<ElementConfig> getAll();
    List<ElementConfig> getMany(@Param("list") List<String> names);
    ElementConfig getOne(@Param("name") String name);

    void insertOne(@Param("elementConfig") ElementConfig elementConfig);
    void insertMany(@Param("list") List<ElementConfig> elementConfigs);

    void updateOne(@Param("elementConfig") ElementConfig elementConfig);
    void updateMany(@Param("list") List<ElementConfig> elementConfigs);

    void deleteOne(@Param("name") String name);
    void deleteMany(@Param("set") Set<String> names);
}
