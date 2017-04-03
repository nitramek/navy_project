package cz.nitramek.vsb;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(staticName = "make")
public class Tuple<FirstType, SecondType> {
    private FirstType first;
    private SecondType second;
}
