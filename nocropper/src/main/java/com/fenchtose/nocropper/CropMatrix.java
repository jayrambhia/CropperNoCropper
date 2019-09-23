package com.fenchtose.nocropper;

public class CropMatrix {
    public final float scale;
    public final float xTrans;
    public final float yTrans;

    public CropMatrix(float scale, float xTrans, float yTrans) {
        this.scale = scale;
        this.xTrans = xTrans;
        this.yTrans = yTrans;
    }
}
