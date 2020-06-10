package cn.hsb.router.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElementData implements Serializable {
    List<String> element_id_list;
    List<String> final_element_id_list;
    Map<String, Object> context;

    public static ElementData create(List<String> elements, List<String> finalElements, Map<String, Object> context) {
        return new ElementData(elements, finalElements, context);
    }
}
