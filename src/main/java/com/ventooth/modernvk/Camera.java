package com.ventooth.modernvk;

import lombok.NoArgsConstructor;
import org.joml.Vector3d;

import static com.ventooth.modernvk.Utils.HALF_PI;

@NoArgsConstructor
public final class Camera {
    public CameraType type = CameraType.firstPerson;
    public double fovY = 65D;
    public double nearPlane = 0.01F;
    public double farPlane = 1000F;
    public final Vector3d forward = new Vector3d(0D, 0D, -1D);
    public final Vector3d right = new Vector3d(1D, 0D, 0D);
    public final Vector3d up = new Vector3d(0D, 1D, 0D);
    public double distance = 3;
    public double yaw = HALF_PI;
    public double pitch = 0;

    public enum CameraType {
        orbit,
        firstPerson
    }
}
