package cz.nitramek.vsb.model;

import java.util.EventListener;


@FunctionalInterface
public interface ValueListener extends EventListener {
    void valueChange(double value);
}
