/*
 * Purpose: A class that simulates a list of bodies and constraints based on physics
 * Original Creation Date: January 1 2016
 * @author Emilio Kartono
 * @version January 15 2016
 */

package com.javaphysicsengine.api;

import com.javaphysicsengine.api.body.PBody;
import com.javaphysicsengine.api.body.PCollidable;
import com.javaphysicsengine.api.body.PConstraints;
import com.javaphysicsengine.api.collision.PCollisionResult;
import com.javaphysicsengine.utils.Vector;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class PWorld {
    // Physic properties about this world
    private static final double GRAVITY_ACCELERATION = -9.81;
    private static final double SCALE = 100;

    // List containing the physical bodies and joints
    private ArrayList<PBody> bodies = new ArrayList<>();
    private ArrayList<PConstraints> constraints = new ArrayList<>();

    /**
     * Post-condition: Returns the list of bodies added to the world
     * @return Returns the list of bodies added to the world
     */
    public ArrayList<PBody> getBodies() {
        return bodies;
    }

    /**
     * Post-condition: Returns the list of constraints added to the world
     * @return Returns the list of constraints added to the world
     */
    public ArrayList<PConstraints> getConstraints() {
        return constraints;
    }

    /**
     * Pre-condition: The "g" must not be null
     * Post-condition: Draws the bodies and constraints to the screen
     * @param g The Graphics Object
     */
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        for (PBody body : bodies)
            body.drawOutline(g, 600);

        // Drawing the constraints
        for (PConstraints constraint : constraints)
            constraint.drawConstraints(g, 600);
    }

    /**
     * Post-condition: Simulates the bodies for a certain time
     * Pre-condition: "timeEllapsed" should be greater than 0
     * @param timeEllapsed The time that has ellapsed in seconds
     */
    public void simulate(double timeEllapsed) {
        // Clear all the forces from all the bodies
        for (PBody body : bodies) {
            body.getNetForce().setX(0);
            body.getNetForce().setY(0);
        }

        // Add the nessessary forces to all the bodies
        addForces();

        // Translate the bodies based on the forces
        translateBodies(timeEllapsed);

        // Check for collisions
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                PBody firstBody = bodies.get(i);
                PBody secondBody = bodies.get(j);

                boolean isCollidable = (firstBody instanceof PCollidable && secondBody instanceof PCollidable) &&
                    (firstBody.isMoving() || secondBody.isMoving());

                if (isCollidable) {
                    PCollidable collidable1 = (PCollidable) firstBody;
                    PCollidable collidable2 = (PCollidable) secondBody;

                    PCollisionResult result = collidable1.hasCollidedWith(collidable2);

                    if (result.isHasCollided()) {
                        firstBody.translate(result.getBody1Mtv());
                        secondBody.translate(result.getBody2Mtv());
                        calculateImpulse(firstBody, secondBody, result.getMtv());
                        positionalCorrection(firstBody, secondBody, result.getMtv());
                    }
                }
            }
        }
    }

    /**
     * Post-condition: Adds the forces to all the bodies
     */
    private void addForces() {
        for (PBody body : bodies) {
            if (!body.isMoving()) {
                continue;
            }

            // Adding gravitational force
            body.getNetForce().setY(body.getNetForce().getY() + GRAVITY_ACCELERATION * body.getMass());
        }

        // Adding forces from constraints
        for (PConstraints constraint : constraints) {
            constraint.addTensionForce();
        }
    }

    /**
     * Post-condition: Translates all the bodies based on a certain time frame
     * Pre-condition: "timeEllapsed" should be greater than 0
     * @param timeEllapsed The time that has ellapsed
     */
    private void translateBodies(double timeEllapsed) {
        for (PBody body : bodies) {
            if (!body.isMoving()) {
                continue;
            }

            // Getting the acceleration from force ( Force = mass * acceleration )
            double accelerationX = body.getNetForce().getX() / body.getMass();
            double accelerationY = body.getNetForce().getY() / body.getMass();

            // Calculating the new velocity ( V2 = V1 + at)
            body.getVelocity().setX(body.getVelocity().getX() + accelerationX * timeEllapsed);
            body.getVelocity().setY(body.getVelocity().getY() + accelerationY * timeEllapsed);

            // Getting the amount to translate by (Velocity = displacement / time)
            double dx = body.getVelocity().getX() * timeEllapsed * SCALE;
            double dy = body.getVelocity().getY() * timeEllapsed * SCALE;

            // Translate the body
            body.translate(new Vector(dx, dy));
        }
    }

    /**
     * Post-condition: Calculates the impulse and applies them to two bodies
     * Pre-condition: "body1", "body2", "mtv" should not be null
     * @param body1 The first body involved in the collision
     * @param body2 The second body involved in the collision
     * @param mtv The MTD of the two bodies
     */
    private void calculateImpulse(PBody body1, PBody body2, Vector mtv) {
        double body1InversedMass = body1.isMoving() ? 1 / body1.getMass() : 0;
        double body2InversedMass = body2.isMoving() ? 1 / body2.getMass() : 0;

        Vector rv = Vector.minus(body2.getVelocity(), body1.getVelocity());
        Vector normal = mtv.normalize();
        double velAlongNormal = normal.dot(rv);

        // Getting the total impulse of the two bodies as a system
        double coefficientOfResitution = 0.8;
        double totalImpulse = -(1.0f + coefficientOfResitution) * velAlongNormal;
        totalImpulse /= (body1InversedMass + body2InversedMass);

        // Apply impulse to each object
        Vector impulse = Vector.multiply(normal, totalImpulse);
        body1.setVelocity(body1.getVelocity().minus(impulse.multiply(body1InversedMass)));
        body2.setVelocity(body2.getVelocity().add(impulse.multiply(body2InversedMass)));
    }

    /**
     * Post-condition: Moves the two bodies by a slight bit after a collision occured (to prevent gittering)
     * Pre-condition: "body1", "body2", "mtv" should not be null
     * @param body1 The first body involved in the collision
     * @param body2 The second body involved in the collision
     * @param mtv The MTD of the two bodies
     */
    private void positionalCorrection(PBody body1, PBody body2, Vector mtv) {
        final double PERCENT = 0.01f; // usually 20% to 80%
        final double SLOP = 0.1f; // usually 0.01 to 0.1

        double body1InversedMass = body1.isMoving() ? 1 / body1.getMass() : 0;
        double body2InversedMass = body2.isMoving() ? 1 / body2.getMass() : 0;

        double penetrationDepth = mtv.norm2();
        Vector normal = mtv.normalize();

        Vector correction = normal.multiply(PERCENT)
                .multiply(Math.max(penetrationDepth - SLOP, 0.0f))
                .multiply(body1InversedMass + body2InversedMass);

        // Move the first body by a certain amount
        body1.translate(correction.multiply(-1 * body1InversedMass));

        // Move the second body by a certain amount
        body2.translate(correction.multiply(body2InversedMass));
    }
}