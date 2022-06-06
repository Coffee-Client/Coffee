/*
 * Copyright (c) 2022 Coffee client, 0x150 and contributors. See copyright file in project root.
 */

package coffee.client.helper.math;

import net.minecraft.util.math.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

@SuppressWarnings("UnusedReturnValue")
public class Matrix4x4 {

    public float a00;
    public float a01;
    public float a02;
    public float a03;
    public float a10;
    public float a11;
    public float a12;
    public float a13;
    public float a20;
    public float a21;
    public float a22;
    public float a23;
    public float a30;
    public float a31;
    public float a32;
    public float a33;

    public Matrix4x4(FloatBuffer floatBuffer) {
        int offset = floatBuffer.position();
        this.a00 = floatBuffer.get(offset);
        this.a01 = floatBuffer.get(offset + 1);
        this.a02 = floatBuffer.get(offset + 2);
        this.a03 = floatBuffer.get(offset + 3);
        this.a10 = floatBuffer.get(offset + 4);
        this.a11 = floatBuffer.get(offset + 5);
        this.a12 = floatBuffer.get(offset + 6);
        this.a13 = floatBuffer.get(offset + 7);
        this.a20 = floatBuffer.get(offset + 8);
        this.a21 = floatBuffer.get(offset + 9);
        this.a22 = floatBuffer.get(offset + 10);
        this.a23 = floatBuffer.get(offset + 11);
        this.a30 = floatBuffer.get(offset + 12);
        this.a31 = floatBuffer.get(offset + 13);
        this.a32 = floatBuffer.get(offset + 14);
        this.a33 = floatBuffer.get(offset + 15);
    }

    public Matrix4x4(float[] floats) {
        this.a00 = floats[0];
        this.a01 = floats[1];
        this.a02 = floats[2];
        this.a03 = floats[3];
        this.a10 = floats[4];
        this.a11 = floats[5];
        this.a12 = floats[6];
        this.a13 = floats[7];
        this.a20 = floats[8];
        this.a21 = floats[9];
        this.a22 = floats[10];
        this.a23 = floats[11];
        this.a30 = floats[12];
        this.a31 = floats[13];
        this.a32 = floats[14];
        this.a33 = floats[15];
    }

    public Matrix4x4() {
        identity();
    }

    public static Matrix4x4 copyFromRowMajor(Matrix4f matrix4f) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = memoryStack.mallocFloat(16);
            matrix4f.write(floatBuffer, true);
            return new Matrix4x4(floatBuffer);
        }
    }

    public static Matrix4x4 copyFromColumnMajor(Matrix4f matrix4f) {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = memoryStack.mallocFloat(16);
            matrix4f.write(floatBuffer, false);
            return new Matrix4x4(floatBuffer);
        }
    }

    public static Matrix4x4 ortho2DMatrix(float left, float right, float bottom, float top, float near, float far) {
        Matrix4x4 matrix4x4 = new Matrix4x4();
        matrix4x4.identity();
        // calculate right matrix elements
        double rm00 = 2.0 / (right - left);
        double rm11 = 2.0 / (top - bottom);
        double rm30 = (right + left) / (left - right);
        double rm31 = (top + bottom) / (bottom - top);
        // perform optimized multiplication
        // compute the last column first, because other columns do not depend on it
        matrix4x4.a30 = (float) (matrix4x4.a00 * rm30 + matrix4x4.a10 * rm31 + matrix4x4.a30);
        matrix4x4.a31 = (float) (matrix4x4.a01 * rm30 + matrix4x4.a11 * rm31 + matrix4x4.a31);
        matrix4x4.a32 = (float) (matrix4x4.a02 * rm30 + matrix4x4.a12 * rm31 + matrix4x4.a32);
        matrix4x4.a33 = (float) (matrix4x4.a03 * rm30 + matrix4x4.a13 * rm31 + matrix4x4.a33);
        matrix4x4.a00 = (float) (matrix4x4.a00 * rm00);
        matrix4x4.a01 = (float) (matrix4x4.a01 * rm00);
        matrix4x4.a02 = (float) (matrix4x4.a02 * rm00);
        matrix4x4.a03 = (float) (matrix4x4.a03 * rm00);
        matrix4x4.a10 = (float) (matrix4x4.a10 * rm11);
        matrix4x4.a11 = (float) (matrix4x4.a11 * rm11);
        matrix4x4.a12 = (float) (matrix4x4.a12 * rm11);
        matrix4x4.a13 = (float) (matrix4x4.a13 * rm11);
        matrix4x4.a20 = -matrix4x4.a20;
        matrix4x4.a21 = -matrix4x4.a21;
        matrix4x4.a22 = -matrix4x4.a22;
        matrix4x4.a23 = -matrix4x4.a23;
        return matrix4x4;
    }

    public static Matrix4x4 projectionMatrix(float width, float height, float fov, float near, float far) {
        Matrix4x4 proj = new Matrix4x4();
        float aspectRatio = width / height;
        float zp = far + near;
        float zm = far - near;
        float a00 = 1 / aspectRatio;
        float a11 = 1;
        float a22 = -zp / zm;
        float a23 = -(2 * far * near) / zm;
        proj.a00 = a00;
        proj.a11 = a11;
        proj.a22 = a22;
        proj.a23 = a23;
        proj.a32 = -1;
        return proj;
    }

    public static Matrix4x4 scale(float x, float y, float z) {
        Matrix4x4 matrix4x4 = new Matrix4x4();
        matrix4x4.a00 = x;
        matrix4x4.a11 = y;
        matrix4x4.a22 = z;
        matrix4x4.a33 = 1.0F;
        return matrix4x4;
    }

    public static Matrix4x4 translate(float x, float y, float z) {
        Matrix4x4 matrix4x4 = new Matrix4x4();
        matrix4x4.a00 = 1.0F;
        matrix4x4.a11 = 1.0F;
        matrix4x4.a22 = 1.0F;
        matrix4x4.a33 = 1.0F;
        matrix4x4.a03 = x;
        matrix4x4.a13 = y;
        matrix4x4.a23 = z;
        return matrix4x4;
    }

    public Matrix4x4 identity() {
        this.a00 = 1;
        this.a01 = 0;
        this.a02 = 0;
        this.a03 = 0;
        this.a10 = 0;
        this.a11 = 1;
        this.a12 = 0;
        this.a13 = 0;
        this.a20 = 0;
        this.a21 = 0;
        this.a22 = 1;
        this.a23 = 0;
        this.a30 = 0;
        this.a31 = 0;
        this.a32 = 0;
        this.a33 = 1;
        return this;
    }

    public Vector3D project(float x, float y, float z, int[] viewport, Vector3D winCoordsDest) {
        float invW = 1.0f / Math.fma(a03, x, Math.fma(a13, y, Math.fma(a23, z, a33)));
        float nx = Math.fma(a00, x, Math.fma(a10, y, Math.fma(a20, z, a30))) * invW;
        float ny = Math.fma(a01, x, Math.fma(a11, y, Math.fma(a21, z, a31))) * invW;
        float nz = Math.fma(a02, x, Math.fma(a12, y, Math.fma(a22, z, a32))) * invW;
        winCoordsDest.setX(Math.fma(Math.fma(nx, 0.5f, 0.5f), viewport[2], viewport[0]));
        winCoordsDest.setY(Math.fma(Math.fma(ny, 0.5f, 0.5f), viewport[3], viewport[1]));
        winCoordsDest.setZ(Math.fma(0.5f, nz, 0.5f));
        return winCoordsDest;
    }

    public Matrix4x4 mul(Matrix4x4 matrix4x4) {
        float nm00 = Math.fma(a00,
                matrix4x4.a00,
                Math.fma(a10, matrix4x4.a01, Math.fma(a20, matrix4x4.a02, a30 * matrix4x4.a03)));
        float nm01 = Math.fma(a01,
                matrix4x4.a00,
                Math.fma(a11, matrix4x4.a01, Math.fma(a21, matrix4x4.a02, a31 * matrix4x4.a03)));
        float nm02 = Math.fma(a02,
                matrix4x4.a00,
                Math.fma(a12, matrix4x4.a01, Math.fma(a22, matrix4x4.a02, a32 * matrix4x4.a03)));
        float nm03 = Math.fma(a03,
                matrix4x4.a00,
                Math.fma(a13, matrix4x4.a01, Math.fma(a23, matrix4x4.a02, a33 * matrix4x4.a03)));
        float nm10 = Math.fma(a00,
                matrix4x4.a10,
                Math.fma(a10, matrix4x4.a11, Math.fma(a20, matrix4x4.a12, a30 * matrix4x4.a13)));
        float nm11 = Math.fma(a01,
                matrix4x4.a10,
                Math.fma(a11, matrix4x4.a11, Math.fma(a21, matrix4x4.a12, a31 * matrix4x4.a13)));
        float nm12 = Math.fma(a02,
                matrix4x4.a10,
                Math.fma(a12, matrix4x4.a11, Math.fma(a22, matrix4x4.a12, a32 * matrix4x4.a13)));
        float nm13 = Math.fma(a03,
                matrix4x4.a10,
                Math.fma(a13, matrix4x4.a11, Math.fma(a23, matrix4x4.a12, a33 * matrix4x4.a13)));
        float nm20 = Math.fma(a00,
                matrix4x4.a20,
                Math.fma(a10, matrix4x4.a21, Math.fma(a20, matrix4x4.a22, a30 * matrix4x4.a23)));
        float nm21 = Math.fma(a01,
                matrix4x4.a20,
                Math.fma(a11, matrix4x4.a21, Math.fma(a21, matrix4x4.a22, a31 * matrix4x4.a23)));
        float nm22 = Math.fma(a02,
                matrix4x4.a20,
                Math.fma(a12, matrix4x4.a21, Math.fma(a22, matrix4x4.a22, a32 * matrix4x4.a23)));
        float nm23 = Math.fma(a03,
                matrix4x4.a20,
                Math.fma(a13, matrix4x4.a21, Math.fma(a23, matrix4x4.a22, a33 * matrix4x4.a23)));
        float nm30 = Math.fma(a00,
                matrix4x4.a30,
                Math.fma(a10, matrix4x4.a31, Math.fma(a20, matrix4x4.a32, a30 * matrix4x4.a33)));
        float nm31 = Math.fma(a01,
                matrix4x4.a30,
                Math.fma(a11, matrix4x4.a31, Math.fma(a21, matrix4x4.a32, a31 * matrix4x4.a33)));
        float nm32 = Math.fma(a02,
                matrix4x4.a30,
                Math.fma(a12, matrix4x4.a31, Math.fma(a22, matrix4x4.a32, a32 * matrix4x4.a33)));
        float nm33 = Math.fma(a03,
                matrix4x4.a30,
                Math.fma(a13, matrix4x4.a31, Math.fma(a23, matrix4x4.a32, a33 * matrix4x4.a33)));
        return new Matrix4x4(new float[] { nm00, nm01, nm02, nm03, nm10, nm11, nm12, nm13, nm20, nm21, nm22, nm23, nm30,
                nm31, nm32, nm33 });
    }

    public Matrix4x4 set(Matrix4x4 matrix4x4) {
        this.a00 = matrix4x4.a00;
        this.a01 = matrix4x4.a01;
        this.a02 = matrix4x4.a02;
        this.a03 = matrix4x4.a03;
        this.a10 = matrix4x4.a10;
        this.a11 = matrix4x4.a11;
        this.a12 = matrix4x4.a12;
        this.a13 = matrix4x4.a13;
        this.a20 = matrix4x4.a20;
        this.a21 = matrix4x4.a21;
        this.a22 = matrix4x4.a22;
        this.a23 = matrix4x4.a23;
        this.a30 = matrix4x4.a30;
        this.a31 = matrix4x4.a31;
        this.a32 = matrix4x4.a32;
        this.a33 = matrix4x4.a33;
        return this;
    }

    public Matrix4x4 multiply(float scalar) {
        this.a00 *= scalar;
        this.a01 *= scalar;
        this.a02 *= scalar;
        this.a03 *= scalar;
        this.a10 *= scalar;
        this.a11 *= scalar;
        this.a12 *= scalar;
        this.a13 *= scalar;
        this.a20 *= scalar;
        this.a21 *= scalar;
        this.a22 *= scalar;
        this.a23 *= scalar;
        this.a30 *= scalar;
        this.a31 *= scalar;
        this.a32 *= scalar;
        this.a33 *= scalar;
        return this;
    }

    public Matrix4x4 add(Matrix4x4 matrix) {
        this.a00 += matrix.a00;
        this.a01 += matrix.a01;
        this.a02 += matrix.a02;
        this.a03 += matrix.a03;
        this.a10 += matrix.a10;
        this.a11 += matrix.a11;
        this.a12 += matrix.a12;
        this.a13 += matrix.a13;
        this.a20 += matrix.a20;
        this.a21 += matrix.a21;
        this.a22 += matrix.a22;
        this.a23 += matrix.a23;
        this.a30 += matrix.a30;
        this.a31 += matrix.a31;
        this.a32 += matrix.a32;
        this.a33 += matrix.a33;
        return this;
    }

    public Matrix4x4 subtract(Matrix4x4 matrix) {
        this.a00 -= matrix.a00;
        this.a01 -= matrix.a01;
        this.a02 -= matrix.a02;
        this.a03 -= matrix.a03;
        this.a10 -= matrix.a10;
        this.a11 -= matrix.a11;
        this.a12 -= matrix.a12;
        this.a13 -= matrix.a13;
        this.a20 -= matrix.a20;
        this.a21 -= matrix.a21;
        this.a22 -= matrix.a22;
        this.a23 -= matrix.a23;
        this.a30 -= matrix.a30;
        this.a31 -= matrix.a31;
        this.a32 -= matrix.a32;
        this.a33 -= matrix.a33;
        return this;
    }

    public float[] toFloatArray() {
        float[] floats = new float[4 * 4];
        floats[0] = this.a00;
        floats[1] = this.a01;
        floats[2] = this.a02;
        floats[3] = this.a03;
        floats[4] = this.a10;
        floats[5] = this.a11;
        floats[6] = this.a12;
        floats[7] = this.a13;
        floats[8] = this.a20;
        floats[9] = this.a21;
        floats[10] = this.a22;
        floats[11] = this.a23;
        floats[12] = this.a30;
        floats[13] = this.a31;
        floats[14] = this.a32;
        floats[15] = this.a33;
        return floats;
    }

    public FloatBuffer toFloatBuffer() {
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = memoryStack.mallocFloat(16);
            floatBuffer.put(0, this.a00);
            floatBuffer.put(1, this.a01);
            floatBuffer.put(2, this.a02);
            floatBuffer.put(3, this.a03);
            floatBuffer.put(4, this.a10);
            floatBuffer.put(5, this.a11);
            floatBuffer.put(6, this.a12);
            floatBuffer.put(7, this.a13);
            floatBuffer.put(8, this.a20);
            floatBuffer.put(9, this.a21);
            floatBuffer.put(10, this.a22);
            floatBuffer.put(11, this.a23);
            floatBuffer.put(12, this.a30);
            floatBuffer.put(13, this.a31);
            floatBuffer.put(14, this.a32);
            floatBuffer.put(15, this.a33);
            return floatBuffer;
        }
    }

    public Matrix4f toMinecraft() {
        Matrix4f matrix4f = new Matrix4f();
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = memoryStack.mallocFloat(16);
            floatBuffer.put(0, this.a00);
            floatBuffer.put(1, this.a01);
            floatBuffer.put(2, this.a02);
            floatBuffer.put(3, this.a03);
            floatBuffer.put(4, this.a10);
            floatBuffer.put(5, this.a11);
            floatBuffer.put(6, this.a12);
            floatBuffer.put(7, this.a13);
            floatBuffer.put(8, this.a20);
            floatBuffer.put(9, this.a21);
            floatBuffer.put(10, this.a22);
            floatBuffer.put(11, this.a23);
            floatBuffer.put(12, this.a30);
            floatBuffer.put(13, this.a31);
            floatBuffer.put(14, this.a32);
            floatBuffer.put(15, this.a33);
            matrix4f.read(floatBuffer, false);
            return matrix4f;
        }
    }
}
