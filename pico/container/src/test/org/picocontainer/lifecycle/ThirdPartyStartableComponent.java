package org.picocontainer.lifecycle;

public class ThirdPartyStartableComponent implements ThirdPartyStartable {
    StringBuilder sb;
    public ThirdPartyStartableComponent(StringBuilder sb) {
        this.sb = sb;
    }

    public void sstart() {
        sb.append("<");
    }

    public void sstop() {
        sb.append(">");
    }

    public void ddispose() {
        sb.append("!");
    }
}

