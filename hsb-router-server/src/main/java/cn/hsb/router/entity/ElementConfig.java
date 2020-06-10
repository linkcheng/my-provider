package cn.hsb.router.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElementConfig {
    private String name;
    private String path;

    public static ElementConfig create(String name, String path) {
        return new ElementConfig(name, path);
    }
}
