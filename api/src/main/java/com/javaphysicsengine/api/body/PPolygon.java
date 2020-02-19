/*
 * Purpose: To represent the basic properties of all polygons
 * Original Creation Date: January 1 2016
 * @author Emilio Kartono
 * @version January 15 2016
 */

package com.javaphysicsengine.api.body;

import com.javaphysicsengine.api.collision.PCircleCircleCollision;
import com.javaphysicsengine.api.collision.PCirclePolyCollision;
import com.javaphysicsengine.api.collision.PCollisionResult;
import com.javaphysicsengine.api.collision.PPolyPolyCollision;
import com.javaphysicsengine.utils.Trigonometry;
import com.javaphysicsengine.utils.Vector;

import java.awt.Graphics;
import java.util.ArrayList;

public class PPolygon extends PBody implements PCollidable {

    private ArrayList<Vector> vertices = new ArrayList<Vector>();
    private PBoundingBox boundingBox;

    /**
     * Constructs the polygon with a given name
     * @param name the name of the polygon
     */
    public PPolygon(String name) {
        super(name);
    }

    /**
     * Makes a hard copy of an existing polygon
     * @param existingPolygon an existing polygon
     */
    public PPolygon(PPolygon existingPolygon) {
        super(existingPolygon);

        // Make a copy of its vertices
        for (Vector vertexCopy : existingPolygon.vertices)
            vertices.add(new Vector(vertexCopy.getX(), vertexCopy.getY()));
        this.computeCenterOfMass();
    }

    /**
     * Gets all the vertices of this polygon
     * @return the vertices of this polygon
     */
    public ArrayList<Vector> getVertices() {
        return vertices;
    }

    /**
     * Gets the bounding box of this polygon
     * @return the bounding box
     */
    public PBoundingBox getBoundingBox() {
        return boundingBox;
    }

    /**
     * Computes the center of mass
     */
    public void computeCenterOfMass() {
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (Vector vertex : vertices) {
            if (vertex.getX() < minX) minX = vertex.getX();
            if (vertex.getX() > maxX) maxX = vertex.getX();
            if (vertex.getY() < minY) minY = vertex.getY();
            if (vertex.getY() > maxY) maxY = vertex.getY();
        }

        getCenterPt().setX((minX + maxX) / 2);
        getCenterPt().setY((minY + maxY) / 2);

        boundingBox = new PBoundingBox(vertices);
    }

    /**
     * Translates the polygon by an amount
     * @param displacement The displacement to move the body by a certain amount
     */
    public void translate(Vector displacement) {
        // Moving all the vertices based on the displacement
        for (Vector vertex : vertices) {
            vertex.setX(vertex.getX() + displacement.getX());
            vertex.setY(vertex.getY() + displacement.getY());
        }

        // Move the centerPt
        getCenterPt().setX(getCenterPt().getX() + displacement.getX());
        getCenterPt().setY(getCenterPt().getY() + displacement.getY());

        // Move the bounding box
        boundingBox.setMinX(boundingBox.getMinX() + displacement.getX());
        boundingBox.setMaxX(boundingBox.getMaxX() + displacement.getX());
        boundingBox.setMinY(boundingBox.getMinY() + displacement.getY());
        boundingBox.setMaxY(boundingBox.getMaxY() + displacement.getY());
    }

    /**
     * Rotates the body.
     * Pre-condition: the angle must be in degrees.
     * @param newAngle The angle of the body
     */
    public void rotate(double newAngle) {
        // Rotate all the vertices around its center of mass
        for (Vector vertex : vertices) {
            // Shifting the vertex so that the centerPt is (0, 0)
            vertex.setX(vertex.getX() - getCenterPt().getX());
            vertex.setY(vertex.getY() - getCenterPt().getY());

            // Getting the angle made by the vertex and the origin
            double betaAngle = Math.abs(Trigonometry.inverseOfTan(vertex.getY() / vertex.getX()));
            double alphaAngle = Trigonometry.convertBetaToThetaAngle(vertex.getX(), vertex.getY(), betaAngle);

            // Getting the new rotated x and y coordinates based on the unit circle
            double angleToRotateBy = alphaAngle - getAngle() + newAngle;
            vertex.setY(Trigonometry.sin(angleToRotateBy) * vertex.norm2() + getCenterPt().getY());
            vertex.setX(Trigonometry.cos(angleToRotateBy) * vertex.norm2() + getCenterPt().getX());
        }

        if (boundingBox != null)
            boundingBox.recomputeBoundaries(vertices);
        super.setAngle(newAngle);
    }

    /**
     * Moves the body to a new center point
     * @param newCenterPt The new center point
     */
    public void move(Vector newCenterPt) {
        // Compute the displacement from the old centerPt to the new centerPt and call the translate()
        Vector displacement = Vector.minus(newCenterPt, getCenterPt());
        translate(displacement);
    }

    /**
     * Draws the bounding box
     * @param g The Graphics Object
     * @param windowHeight The height of the window that is containing the body being displayed
     */
    @Override
    public void drawBoundingBox(Graphics g, int windowHeight) {
        boundingBox.drawBoundingBox(g, windowHeight);
        super.drawBoundingBox(g, windowHeight);
    }

    /**
     * Draws the color of the object
     * @param g The Graphics Object
     * @param windowHeight The height of the window containing the body being displayed
     */
    @Override
    public void drawFill(Graphics g, int windowHeight) {
        // Convert the vertices to x and y coordinates
        int[] xCoords = new int[vertices.size()];
        int[] yCoords = new int[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            xCoords[i] = (int) vertices.get(i).getX();
            yCoords[i] = windowHeight - (int) vertices.get(i).getY();
        }

        // Draw the polygon onto the screen
        g.setColor(getFillColor());
        g.fillPolygon(xCoords, yCoords, xCoords.length);

        // Draw the center of mass
        super.drawFill(g, windowHeight);
    }

    /**
     * Draws the outline of the polygon
     * @param g The Graphics Object
     * @param windowHeight The height of the window containing the body being displayed
     */
    @Override
    public void drawOutline(Graphics g, int windowHeight) {
        // Convert the vertices to x and y coordinates
        int[] xCoords = new int[vertices.size()];
        int[] yCoords = new int[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            xCoords[i] = (int) vertices.get(i).getX();
            yCoords[i] = windowHeight - (int) vertices.get(i).getY();
        }

        // Draw the polygon onto the screen
        g.setColor(this.getOutlineColor());
        g.drawPolygon(xCoords, yCoords, xCoords.length);

        // Draw the center of mass
        super.drawOutline(g, windowHeight);

        // Draw the normals
        for (int i = 0; i < vertices.size(); i++) {
            Vector sidePt1 = vertices.get(i);
            Vector sidePt2 = i + 1 < vertices.size() ? vertices.get(i + 1) : vertices.get(0);

            Vector midPt = sidePt1.add(sidePt2).multiply(0.5);

            Vector normal = Vector.of(sidePt2.getY() - sidePt1.getY(), -1 * (sidePt2.getX() - sidePt1.getX())).normalize();
            Vector endPt = normal.multiply(10).add(midPt);

            int x1 = (int) midPt.getX();
            int y1 = windowHeight - (int) midPt.getY();
            int x2 = (int) endPt.getX();
            int y2 = windowHeight - (int) endPt.getY();

            g.setColor(this.getNormalVectorColor());
            g.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * Returns the polygon as a string.
     * It should be used only for debugging purposes.
     * @return the polygon as a string.
     */
    @Override
    public String toString() {
        StringBuilder propertiesLine = new StringBuilder(super.toString() + "Vertices:");
        for (int i = 0; i < vertices.size(); i++) {
            propertiesLine.append(vertices.get(i).getX())
                    .append(" ")
                    .append(vertices.get(i).getY());

            if (i < vertices.size() - 1)
                propertiesLine.append(",");
        }
        return propertiesLine.toString();
    }

    @Override
    public PCollisionResult hasCollidedWith(PCollidable body) {
        PCollisionResult result;

        if (body instanceof PCircle) {
            result = PCirclePolyCollision.doBodiesCollide((PCircle) body, this);

            // Note: since we are not comparing this obj with the incoming obj, the directions are flipped
            result = new PCollisionResult(result.isHasCollided(), result.getBody2Mtv(),
                    result.getBody1Mtv(), result.getMtv().multiply(-1));

        } else if (body instanceof PPolygon) {
            result = PPolyPolyCollision.doBodiesCollide(this, (PPolygon) body);

        } else {
            throw new IllegalArgumentException("Body cannot detect and handle collisions!");
        }

        return result;
    }
}