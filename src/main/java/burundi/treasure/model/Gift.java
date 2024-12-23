package burundi.treasure.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Gift {
    private String id;
    private String name;
    private long noItem;
    private String type;
    private double probability;
}
