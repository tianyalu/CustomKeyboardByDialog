package com.sty.learn.keyboard.dialog;

/**
 * Utility class.
 */
public final class LPUtils {

    public static int screenWidth_ = 320;
    public static int screenHeight_ = 480;

    // 界面参考缩放参数
    static float SCALEDATE;

    public LPUtils() {

    }

    public static int getScaledValue(int num) {
        if (SCALEDATE != 0) {
            num = (int) (SCALEDATE * num);
        }
        return num;
    }

    /**
     *
     * @param screenWidth
     *            参照宽度
     */
    public static void setScaledParams(float screenWidth) {
        SCALEDATE = screenWidth;
    }

}