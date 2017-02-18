package cz.nitramek.vsb.model;

import java.util.EventListener;


@FunctionalInterface
public interface ValueChangedListener extends EventListener {
    void valueChange(double value);
}
