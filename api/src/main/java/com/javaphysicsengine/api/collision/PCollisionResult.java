package com.javaphysicsengine.api.collision;

import com.javaphysicsengine.utils.Vector;

/**
 * Stores the result of a collision
 * Note that the mtv must go in the direction of body1Mtv
 */
public class PCollisionResult {
    private final boolean hasCollided;
    private final Vector body1Mtv;
    private final Vector body2Mtv;
    private final Vector mtv;
    private final Vector contactPt;

    public PCollisionResult(boolean hasCollided, Vector body1Mtv, Vector body2Mtv, Vector mtv) {
        this.hasCollided = hasCollided;
        this.body1Mtv = body1Mtv;
        this.body2Mtv = body2Mtv;
        this.mtv = mtv;
        this.contactPt = Vector.of(0, 0);
    }

    public PCollisionResult(boolean hasCollided, Vector body1Mtv, Vector body2Mtv, Vector mtv, Vector contactPt) {
        this.hasCollided = hasCollided;
        this.body1Mtv = body1Mtv;
        this.body2Mtv = body2Mtv;
        this.mtv = mtv;
        this.contactPt = contactPt;
    }

    public boolean isHasCollided() {
        return hasCollided;
    }

    public Vector getBody1Mtv() {
        return body1Mtv;
    }

    public Vector getBody2Mtv() {
        return body2Mtv;
    }

    public Vector getMtv() {
        return mtv;
    }

    public Vector getContactPt() {
        return contactPt;
    }
}
