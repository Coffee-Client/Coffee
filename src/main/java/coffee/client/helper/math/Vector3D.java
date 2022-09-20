/*
 * Copyright (c) 2022 Coffee Client, 0x150 and contributors.
 * Some rights reserved, refer to LICENSE file.
 */

package coffee.client.helper.math;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class Vector3D {

    public double x, y, z;

    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector3D(Vec3d vec3d) {
        this.x = vec3d.x;
        this.y = vec3d.y;
        this.z = vec3d.z;
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Vector3D multiply(double mulX, double mulY, double mulZ) {
        this.x *= mulX;
        this.y *= mulY;
        this.z *= mulZ;
        return this;
    }

    public Vector3D divide(double divX, double divY, double divZ) {
        this.x /= divX;
        this.y /= divY;
        this.z /= divZ;
        return this;
    }

    public Vector3D add(double addX, double addY, double addZ) {
        this.x += addX;
        this.y += addY;
        this.z += addZ;
        return this;
    }

    public Vector3D subtract(double subX, double subY, double subZ) {
        this.x -= subX;
        this.y -= subY;
        this.z -= subZ;
        return this;
    }

    public Vector3D transform(Matrix4f matrix4f) {
        return transform(Matrix4x4.copyFromRowMajor(matrix4f));
    }

    public Vector3D transform(Matrix4x4 matrix4x4) {
        double f = this.x;
        double g = this.y;
        double h = this.z;
        this.x = matrix4x4.a00 * f + matrix4x4.a01 * g + matrix4x4.a02 * h + matrix4x4.a03;
        this.y = matrix4x4.a10 * f + matrix4x4.a11 * g + matrix4x4.a12 * h + matrix4x4.a13;
        this.z = matrix4x4.a20 * f + matrix4x4.a21 * g + matrix4x4.a22 * h + matrix4x4.a23;
        return this;
    }

    public Vector3D multiply(Vector3D vector3D) {
        return multiply(vector3D.getX(), vector3D.getY(), vector3D.getZ());
    }

    public Vector3D divide(Vector3D vector3D) {
        return divide(vector3D.getX(), vector3D.getY(), vector3D.getZ());
    }

    public Vector3D add(Vector3D vector3D) {
        return add(vector3D.getX(), vector3D.getY(), vector3D.getZ());
    }

    public Vector3D subtract(Vector3D vector3D) {
        return subtract(vector3D.getX(), vector3D.getY(), vector3D.getZ());
    }

    public Vector3D multiply(double mul) {
        this.x *= mul;
        this.y *= mul;
        this.z *= mul;
        return this;
    }

    public Vector3D divide(double div) {
        this.x /= div;
        this.y /= div;
        this.z /= div;
        return this;
    }

    public Vector3D add(double add) {
        this.x += add;
        this.y += add;
        this.z += add;
        return this;
    }

    public Vector3D subtract(double sub) {
        this.x -= sub;
        this.y -= sub;
        this.z -= sub;
        return this;
    }

    public Vec3d toMinecraft() {
        return new Vec3d(x, y, z);
    }
}
