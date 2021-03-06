package com.javaphysicsengine.api.body;

import com.javaphysicsengine.utils.Vector;

import java.awt.Color;
import java.awt.Graphics;

public class PString extends PConstraints {
    /**
     * Pre-condition: "body1" and "body2" must not be null
     * Post-condition: Creates a PString object with "body1" and "body2" attached
     * @param body1 The first body to be attached to.
     * @param body2 The second body to be attached to.
     */
    public PString(PBody body1, PBody body2) {
        super(body1, body2);

        // Compute the distance between the two center pts of the two bodies
        double bodyDist = Math.sqrt(Math.pow(body1.getCenterPt().getX() - body2.getCenterPt().getX(), 2) + Math.pow(body1.getCenterPt().getY() - body2.getCenterPt().getY(), 2));
        setLength(bodyDist);
        System.out.println("Distance Set: " + bodyDist);
    }

    /**
     * Post-condition: Adds the tension forces to the attached bodies
     */
    public void addTensionForce() {
        // Check if the distance between the strings are greater than the string length
        PBody[] bodies = super.getAttachedBodies();

        // Computing the length of the string
        double lengthPerBody = getLength() / 2.0;
        System.out.println("L:" + getLength());

        if (!bodies[0].isMoving() || !bodies[1].isMoving())
            lengthPerBody = getLength();

        double equilPt_X = (bodies[0].getCenterPt().getX() + bodies[1].getCenterPt().getX()) / 2;
        double equilPt_Y = (bodies[0].getCenterPt().getY() + bodies[1].getCenterPt().getY()) / 2;
        if (!bodies[0].isMoving()) {
            equilPt_X = bodies[0].getCenterPt().getX();
            equilPt_Y = bodies[0].getCenterPt().getY();
        } else if (!bodies[1].isMoving()) {
            equilPt_X = bodies[1].getCenterPt().getX();
            equilPt_Y = bodies[1].getCenterPt().getY();
        }


        for (PBody body : bodies) {
            double distX = equilPt_X - body.getCenterPt().getX();
            double distY = equilPt_Y - body.getCenterPt().getY();
            double distance = Math.sqrt((distX * distX) + (distY * distY));

            if (distance > lengthPerBody) {
                double centripetalForce_Scalar = body.getMass() * Math.pow(body.getVelocity().norm2(), 2) / lengthPerBody;
                Vector centripetalForce_Vector = new Vector(-distX, -distY);
                centripetalForce_Vector.setLength(centripetalForce_Scalar);

                body.setNetForce(centripetalForce_Vector);

                // Move the body to the circumference of the rope
                if (body.isMoving()) {
                    Vector displacement = new Vector(distX, distY);
                    displacement.setLength(distance - lengthPerBody);
                    displacement.setXY(displacement.getX() * -1, displacement.getY() * -1);
                    body.translate(displacement);
                }
            }
        }
    }

    /**
     * Pre-condition: The "g" must not be null and the "windowHeight" must be greater than 0
     * Post-condition: Draws a line between the two attached bodies
     * @param g The Graphics Object
     * @param windowHeight The height of the window that is containing the body being displayed
     */
    public void drawConstraints(Graphics g, int windowHeight) {
        // Draw a line in between the two objects
        g.setColor(Color.WHITE);
        PBody[] bodies = super.getAttachedBodies();
        g.drawLine((int) bodies[0].getCenterPt().getX(), windowHeight - (int) bodies[0].getCenterPt().getY(),
                (int) bodies[1].getCenterPt().getX(), windowHeight - (int) bodies[1].getCenterPt().getY());
    }
}