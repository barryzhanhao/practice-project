package pers.james.practice.mapdb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyData implements java.io.Serializable {
    public String name;
    public int number;

}
