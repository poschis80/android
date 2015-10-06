package xpshome.net.util;

import android.os.Build;
import android.test.suitebuilder.annotation.Suppress;

import java.lang.reflect.Field;

import cc.catalysts.kapsch.mcc_android.BuildConfig;

/**
 * Created by Christian Poschinger on 06.10.2015.
 *
 * To use the compat, recycle, design version checker you have to define the version numbers inside
 * the build.gradle file as a custom build config field.
 * Check the example build.gradle file for on simple how to.
 *
 * If you call one of the following methods without the corresponding defined config field a NoSuchFieldException will be thrown.
 * compatVersion(...) -> definition in grade file : buildConfigField "String","COMPAT_VERSION","$CompatVersion"
 * designVersion(...) -> definition in grade file : buildConfigField "String","DESIGN_VERSION","$DesignVersion"
 * recycleVersion(...) -> definition in grade file : buildConfigField "String","RECYCLE_VERSION","$RecycleVersion"
 *
 */
public class VersionCheck {
    public static final int LatestVersion = -1;
    private static final String F_COMPAT_VERSION = "COMPAT_VERSION";
    private static final String F_DESIGN_VERSION ="DESIGN_VERSION";
    private static final String F_RECYCLE_VERSION = "RECYCLE_VERSION";

    private static boolean checkEqual(int left, int right) {
        return left == right;
    }
    private static boolean checkLess(int left, int right) {
        return left < right;
    }
    private static boolean checkGreater(int left, int right) {
        return left > right;
    }

    private static <T> T getField(final String fieldName) throws NoSuchFieldException, ClassCastException, IllegalAccessException {
        BuildConfig bf = new BuildConfig();
        Field f = BuildConfig.class.getDeclaredField(F_COMPAT_VERSION);
        return (T)f.get(bf);
    }

    private static boolean check(Action action, int versionL, int versionR) {
        if (versionL == LatestVersion || versionR == LatestVersion) {  // if at least one of the version numbers ist set to LatestVersion we will always return true;
            return true;
        }

        switch (action) {
            case EQUAL: {
                return checkEqual(versionL, versionR);
            }
            case EQUAL_OR_GREATER: {
                return checkEqual(versionL, versionR) || checkGreater(versionL, versionR);
            }
            case EQUAL_OR_LESS: {
                return checkEqual(versionL, versionR) || checkLess(versionL, versionR);
            }
            case LESS: {
                return checkLess(versionL, versionR);
            }
            case GREATER: {
                return checkGreater(versionL, versionR);
            }
        }
        return false;
    }

    // TODO : improve still not working as expected
    private static boolean checkPairVersions(Action action, int left, int right, int vleft, int vright) {
        switch (action) {
            case EQUAL:
            case GREATER:
            case LESS:
                if (check(Action.EQUAL, left, vleft) && check(action, right, vright)) {
                    return true;
                } else
                return check(action, left, vleft) && check(action, right, vright);
            case EQUAL_OR_LESS:
                if (check(Action.LESS, left, vleft)) {
                    return true;
                }
                return check(action, left, vleft) && check(action, right, vright);
            case EQUAL_OR_GREATER:
                if (check(Action.GREATER, left, vleft)) {
                    return true;
                }
                return check(action, left, vleft) && check(action, right, vright);
        }
        return false;
    }

    private static String[] preparePairVersionNumber(String version) {
        if (version != null && !version.isEmpty()) {
            String[] parts = version.split(";");
            if (parts != null && parts.length > 0) {
                String[] p = new String[]{"-1", "-1"};
                p[0] = parts[0].isEmpty() == false ? parts[0] : p[0];
                if (p.length >= 2) {
                    p[1] = parts[1].isEmpty() == false ? parts[1] : p[1];
                }
                return p;
            }
        }
        return null;
    }

    public enum Action {
        EQUAL,
        LESS,
        GREATER,
        EQUAL_OR_LESS,
        EQUAL_OR_GREATER,
    }

    public static boolean version(Action action, int versionLeft, int versionRigh) {
        return check(action, versionLeft, versionRigh);
    }

    public static boolean apiLevel(Action action, int version) {
        return check(action, version, Build.VERSION.SDK_INT);
    }

    public static boolean compatVersion(Action action, int left, int right) throws NoSuchFieldException, ClassCastException, IllegalAccessException {
        String[] s = preparePairVersionNumber((String)getField(F_COMPAT_VERSION));
        if (s != null) {
            return checkPairVersions(action, left, right, Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        }
        return false;
    }

    public static boolean designVersion(Action action, int version)  throws NoSuchFieldException, ClassCastException, IllegalAccessException {
        String s = getField(F_DESIGN_VERSION);
        if (s != null && !s.isEmpty()) {
            return check(action, version, Integer.parseInt(s));
        }
        return false;
    }

    public static boolean recycleVersion(Action action, int left, int right)  throws NoSuchFieldException, ClassCastException, IllegalAccessException {
        String[] s = preparePairVersionNumber((String) getField(F_RECYCLE_VERSION));
        if (s != null) {
            return checkPairVersions(action, left, right, Integer.parseInt(s[0]), Integer.parseInt(s[1]));
        }
        return false;
    }
}
