package com.github.exobite.mc.zombifyvillagers.utils;

import org.bukkit.Bukkit;

import java.util.Locale;

@SuppressWarnings("unused")
public class VersionHelper {

    private VersionHelper() {}

    public static Version getVersionFromString(String version) {
        if(Utils.countMatches(version, ".") < 1) {
            return new Version(0, 0, 0);
        }
        String[] versionSplits = version.split("\\.");
        int major;
        int minor;
        int patch = 0;
        major = Integer.parseInt(versionSplits[0]);
        minor = Integer.parseInt(versionSplits[1]);
        if(versionSplits.length>=3) patch = Integer.parseInt(versionSplits[2]);
        return new Version(major, minor, patch);
    }

    public static Version getBukkitVersion(){
        String bukkitV = Bukkit.getBukkitVersion();
        if(!bukkitV.contains("-")) {
            throw new NullPointerException("Can't analyse Bukkit Version: "+bukkitV);
        }
        return getVersionFromString(bukkitV.split("-")[0]);
    }

    public static Version getBukkitVersionNoPatch(){
        Version bukkitVer = getBukkitVersion();
        return new Version(bukkitVer.major(), bukkitVer.minor(), 0);
    }

    public static boolean isPaper() {
        return Bukkit.getVersion().toLowerCase(Locale.ROOT).contains("paper");
    }


    /**
     * Checks if the two Version are equal
     * @param v1 The first Version
     * @param v2 The second Version
     * @return true if both Versions are equal
     */
    public static boolean isEqual(Version v1, Version v2) {
        return v1.major()==v2.major() && v1.minor()==v2.minor() && v1.patch()==v2.patch();
    }

    /**
     * Returns whether the Param 'isLarger' is Larger than 'isSmaller'
     * @param isLarger The Version to check if it's Larger
     * @param isSmaller The Version to check against
     * @return true if Param 1 is Larger than Param 2
     */
    public static boolean isLarger(Version isLarger, Version isSmaller) {
        return (isLarger.major()==isSmaller.major() && isLarger.minor()==isSmaller.minor() && isLarger.patch()>isSmaller.patch()) ||
                (isLarger.major()==isSmaller.major() && isLarger.minor()>isSmaller.minor()) ||
                (isLarger.major()>isSmaller.major());
    }

    /**
     * Returns whether the Param 'isLarger' is Larger or equal than the Param 'isSmaller'
     * @param isLarger The Version to check if its Larger or Equal
     * @param isSmaller The Version to check against
     * @return true if Param 1 is Larger or Equal to Param 2
     */
    public static boolean isEqualOrLarger(Version isLarger, Version isSmaller) {
        return isLarger(isLarger, isSmaller) || isEqual(isLarger, isSmaller);
    }

    /**
     * Returns whether the Param 'isSmaller' is smaller than 'isLarger'
     * @param isSmaller The Version to check if it's smaller
     * @param isLarger The Version to check against
     * @return true if Param 1 is Smaller than Param 2
     */
    public static boolean isSmaller(Version isSmaller, Version isLarger) {
        return (isSmaller.major()==isLarger.major() && isSmaller.minor()==isLarger.minor() && isSmaller.patch()<isLarger.patch()) ||
                (isSmaller.major()==isLarger.major() && isSmaller.minor()<isLarger.minor()) ||
                (isSmaller.major()<isLarger.major());
    }

    /**
     * Returns whether the Param 'isSmaller' is smaller or equal than the Param 'isLarger'
     * @param isLarger The Version to check if its smaller or Equal
     * @param isSmaller The Version to check against
     * @return true if Param 1 is smaller or Equal to Param 2
     */
    public static boolean isEqualOrSmaller(Version isSmaller, Version isLarger) {
        return isSmaller(isSmaller, isLarger) || isEqual(isSmaller, isLarger);
    }




}
