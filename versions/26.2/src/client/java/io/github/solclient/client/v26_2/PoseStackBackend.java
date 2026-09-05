package io.github.solclient.client.v26_2;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.solclient.client.platform.RenderBackend;
import org.joml.Quaternionf;

public final class PoseStackBackend implements RenderBackend {

    private final PoseStack pose;

    public PoseStackBackend(PoseStack pose) {
        this.pose = pose;
    }

    @Override
    public void pushMatrix() {
        pose.pushPose();
    }

    @Override
    public void popMatrix() {
        pose.popPose();
    }

    @Override
    public void translate(double x, double y, double z) {
        pose.translate(x, y, z);
    }

    @Override
    public void scale(double x, double y, double z) {
        pose.scale((float) x, (float) y, (float) z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        pose.mulPose(new Quaternionf().rotateAxis((float) Math.toRadians(angle), x, y, z));
    }
}
