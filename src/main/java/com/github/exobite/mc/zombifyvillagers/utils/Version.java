package com.github.exobite.mc.zombifyvillagers.utils;

/*public record Version(int major,
                      int minor,
                      int patch) {
}*/

public class Version {

    private final int major;
    private final int minor;
    private final int patch;

    private boolean hideMinor;
    private boolean hidePatch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version hidePatch(boolean hidePatch) {
        this.hidePatch = hidePatch;
        return this;
    }

    public Version hideMinor(boolean hideMinor) {
        this.hideMinor = hideMinor;
        return this;
    }

    protected int major() {
        return major;
    }

    protected int minor() {
        return minor;
    }

    protected int patch() {
        return patch;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("v").append(major).append(".");
        if(hideMinor) sb.append("*");
        else sb.append(minor);
        sb.append(".");
        if(hidePatch) sb.append("*");
        else sb.append(patch);
        return sb.toString();
    }


}
