package cz.nitramek.vsb;

import lombok.Data;

@Data(staticConstructor = "make")
public class Tuple<FirstType, SecondType> {
    private final FirstType first;
    private final SecondType second;
}
