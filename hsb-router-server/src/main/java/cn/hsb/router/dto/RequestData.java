package cn.hsb.router.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestData implements Serializable{
    String path;
    ElementData data;

    public static RequestData create(String path, ElementData data) {
        return new RequestData(path, data);
    }
}
