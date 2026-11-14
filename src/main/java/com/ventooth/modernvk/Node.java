package com.ventooth.modernvk;

import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class Node {
    private final Vector3d m_translation = new Vector3d();
    private final Vector3d m_scale = new Vector3d(1);
    private final Quaterniond m_rotation = new Quaterniond();
    private final Matrix4d m_transform = new Matrix4d();

    private boolean m_dirty = true;

    public String name;

    public int meshId = 0;
    public int lightId = 0;
    public int parentId = 0;
    public int nextSiblingId = 0;
    public int firstChildId = 0;

    public Vector3dc getTranslation() {
        return m_translation;
    }

    public void setTranslation(Vector3dc translation) {
        m_translation.set(translation);
        m_dirty = true;
    }

    public Quaterniondc getRotation() {
        return m_rotation;
    }

    public void setRotation(Quaterniondc rotation) {
        m_rotation.set(rotation);
        m_dirty = true;
    }

    public Vector3dc getScale() {
        return m_scale;
    }

    public void setScale(Vector3dc scale) {
        m_scale.set(scale);
        m_dirty = true;
    }

    public Matrix4dc getTransform() {
        if (m_dirty) {
            // recalculate the local transform matrix
            m_transform.identity();
            m_transform.translate(m_translation);
            m_transform.rotate(m_rotation);
            m_transform.scale(m_scale);
            m_dirty = false;
        }
        return m_transform;
    }

    public void setTransform(Matrix4dc transform) {
        // TODO: Might be broken :(
        transform.getTranslation(m_translation);
        transform.getNormalizedRotation(m_rotation);
        transform.getScale(m_scale);

        m_transform.set(transform);
        m_dirty = false;
    }

    @Override
    public String toString() {
        return name;
    }
}
