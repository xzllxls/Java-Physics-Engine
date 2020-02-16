/*
 * Purpose: To detect whether two polygons collide with each other (using SAT algorithm)
 * Original Creation Date: January 1 2016
 * @author Emilio Kartono
 * @version January 15 2016
 */

package com.javaphysicsengine.api.collision;

import com.javaphysicsengine.api.body.PPolygon;
import com.javaphysicsengine.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public class PPolyPolyCollision {

    /**
     * Post-condition: Returns true if a separating line exist between the two polygons based on a normal.
     *                  Also returns the MTD from the normal if there is no separating line
     * Pre-condition: "bestOverlap" must not be null
     * @param normalSlope The slope of the normal
     * @param bestOverlap The MTD from the normal
     * @return Returns true if there is a separating line between the two polygons based on a normal. Also returns the MTD from the "bestOverlap" parameter
     */
    private static boolean isSeparatingLineExist(List<Vector> poly1Vertices, List<Vector> poly2Vertices, double normalSlope, Vector bestOverlap) {
        // Storing the min/max x and y POI coordinates of poly1
        Vector min1Values = new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
        Vector max1Values = new Vector(-Double.MIN_VALUE, -Double.MIN_VALUE);

        for (Vector vertex : poly1Vertices) {
            // Getting the projected point of a vertex to the normal
            Vector poi = PCollisionUtil.projectPointToLine(normalSlope, 13, vertex);

            // Checking if the current POI is the new min/max x and y coordinate
            if (poi.getX() < min1Values.getX()) min1Values.setX(poi.getX());
            if (poi.getY() < min1Values.getY()) min1Values.setY(poi.getY());
            if (poi.getX() > max1Values.getX()) max1Values.setX(poi.getX());
            if (poi.getY() > max1Values.getY()) max1Values.setY(poi.getY());
        }

        // Storing the min/max x and y POI coordinates of poly2
        Vector min2Values = new Vector(Double.MAX_VALUE, Double.MAX_VALUE);
        Vector max2Values = new Vector(-Double.MIN_VALUE, -Double.MIN_VALUE);

        for (Vector vertex : poly2Vertices) {
            // Getting the projected point of a vertex to the normal
            Vector poi = PCollisionUtil.projectPointToLine(normalSlope, 13, vertex);

            // Checking if the current POI is the new min/max x and y coordinate
            if (poi.getX() < min2Values.getX()) min2Values.setX(poi.getX());
            if (poi.getY() < min2Values.getY()) min2Values.setY(poi.getY());
            if (poi.getX() > max2Values.getX()) max2Values.setX(poi.getX());
            if (poi.getY() > max2Values.getY()) max2Values.setY(poi.getY());
        }

        return !PCollisionUtil.doDomainsIntersect(min1Values, max1Values, min2Values, max2Values, bestOverlap);
    }

    /**
     * Post-condition: Returns true if the two polygons are intersecting; else false.
     *                  Also returns the MTD of the two polygons if they are interesecting
     * @param mtd The MTD (minimum translation vector) of the two polygons
     * @return Returns whether the two polygons are intersecting; and the MTD stored in the parameter "mtd"
     */
    private static boolean isIntersecting(List<Vector> poly1Vertices, List<Vector> poly2Vertices, Vector mtd) {
        mtd.setXY(0, 0); // Set MTD to 0 (just in case it is not intersecting)
        Vector bestOverlap = null;
        double bestOverlapDistance = Double.MAX_VALUE;

        // Going through each side in poly1 and see if poly2 intersects it
        // // System.out.println("Polygon 1:");
        for (int i = 0; i < poly1Vertices.size(); i++) {
            // Getting the two points that make up a side
            int sidePt1Index = i;
            int sidePt2Index = i + 1;
            if (i == poly1Vertices.size() - 1)
                sidePt2Index = 0;

            // Getting the normal slope
            Vector sidePt1 = poly1Vertices.get(sidePt1Index);
            Vector sidePt2 = poly1Vertices.get(sidePt2Index);
            double normalSlope = -1 * ((sidePt2.getY() - sidePt1.getY()) / (sidePt2.getX() - sidePt1.getX()));

            // // System.out.println("  SP1:" + sidePt1 + " | SP2:" + sidePt2 + " | NS:" + normalSlope);

            // Getting the current overlap from the current side
            Vector curBestOverlap = new Vector(0, 0);
            if (isSeparatingLineExist(poly1Vertices, poly2Vertices, normalSlope, curBestOverlap))  // <- SAT algorithm: If there is a separating line between the polygons, there is no collision
                return false;

            // Checking if the current overlap is the best overlap
            double curBestOverlapDistance = (curBestOverlap.getX() * curBestOverlap.getX()) + (curBestOverlap.getY() * curBestOverlap.getY());

            // // System.out.println("  CBO:" + curBestOverlap);

            if (curBestOverlapDistance < bestOverlapDistance) {
                bestOverlapDistance = curBestOverlapDistance;
                bestOverlap = curBestOverlap;
            }
        }

        // Going through each side in poly2 and see if poly1 intersects it
        for (int i = 0; i < poly2Vertices.size(); i++) {
            // Getting the two points that make up a side
            int sidePt1Index = i;
            int sidePt2Index = i + 1;
            if (i == poly2Vertices.size() - 1)
                sidePt2Index = 0;

            // Getting the normal slope
            Vector sidePt1 = poly2Vertices.get(sidePt1Index);
            Vector sidePt2 = poly2Vertices.get(sidePt2Index);
            double normalSlope = -1 / ((sidePt2.getY() - sidePt1.getY()) / (sidePt2.getX() - sidePt1.getX()));

            // Getting the current overlap from the current side
            Vector curBestOverlap = new Vector(0, 0);
            if (isSeparatingLineExist(poly1Vertices, poly2Vertices, normalSlope, curBestOverlap))  // <- SAT algorithm: If there is a separating line between the polygons, there is no collision
                return false;

            // Checking if the current overlap is the best overlap
            double curBestOverlapDistance = (curBestOverlap.getX() * curBestOverlap.getX()) + (curBestOverlap.getY() * curBestOverlap.getY());

            if (curBestOverlapDistance < bestOverlapDistance) {
                bestOverlapDistance = curBestOverlapDistance;
                bestOverlap = curBestOverlap;
            }
        }

        if (bestOverlap == null) {
            return false;
        }

        mtd.setXY(bestOverlap.getX(), bestOverlap.getY());
        return true;
    }

    /**
     * Post-condition: Returns the displacement the main circle should move by
     * Pre-condition: "mtd", "mainPolyCenterPt", "otherPolyCenterPt", "mainPolyVelocity", "otherPolyVelocity" must not be null
     * @param mtd The minimum translation vector from SAT algorithm
     * @param mainPolyCenterPt The center point of the main circle
     * @param otherPolyCenterPt The center point of the other circle
     * @param mainPolyVelocity The velocity of the main circle
     * @param otherPolyVelocity The velocity of the other circle
     * @return Returns the displacement the main circle should move by
     */
    protected static Vector getTranslationVectors(Vector mtd, Vector mainPolyCenterPt, Vector otherPolyCenterPt, Vector mainPolyVelocity, Vector otherPolyVelocity) {
        // Checking if the velocity of main polygon is 0
        if (mainPolyVelocity.getX() == 0 && mainPolyVelocity.getY() == 0)
            return new Vector(0, 0);

        // Making sure the push vector is pushing the polygons away
        Vector translationVector = new Vector(mtd.getX(), mtd.getY());

        Vector displacementBetweenPolygons = Vector.minus(mainPolyCenterPt, otherPolyCenterPt);
        if (Vector.dot(displacementBetweenPolygons, mtd) < 0) {
            // // System.out.println("I am here!");
            translationVector.setX(translationVector.getX() * -1);
            translationVector.setY(translationVector.getY() * -1);
        }

        // Get the ratio of the translation vector when both objects are moving
        double curLength = translationVector.norm2();
        double lengthOfMainVelocity = mainPolyVelocity.norm2();
        double lengthOfOtherVelocity = otherPolyVelocity.norm2();
        double newLength = curLength * (lengthOfMainVelocity / (lengthOfMainVelocity + lengthOfOtherVelocity));

        translationVector.setLength(newLength);

        // Checking if the new translation vector is a null (happens if the length is a 0)
        if (Double.isNaN(translationVector.getX()) && Double.isNaN(translationVector.getY()))
            translationVector.setXY(0, 0);

        return translationVector;
    }

    /**
     * Pre-condition: "body1", "body2", "body1TransVector", "body2TransVector", "mtd" must not be null
     * Post-condition: Determines whether two polygons are colliding, and returns the displacements each polygon should move by as well as the minimum translation vector
     * @param body1 The first polygon
     * @param body2 The second polygon
     * @param body1TransVector The displacement the circle should move by if they are colliding
     * @param body2TransVector The displacement the polygon should move by if they are colliding
     * @param mtd Returns the minimum translation vector from SAT algorithm
     * @return Returns true if the two polygons are colliding; else false. Also returns the MTD which is stored in the "mtd" parameter
     */
    public static boolean doBodiesCollide(PPolygon body1, PPolygon body2, Vector body1TransVector, Vector body2TransVector, Vector mtd) {

        ArrayList<Vector> poly1Vertices = body1.getVertices();
        ArrayList<Vector> poly2Vertices = body2.getVertices();

        Vector poly1CenterPt = body1.getCenterPt();
        Vector poly2CenterPt = body2.getCenterPt();

        Vector poly1Velocity = body1.getVelocity();
        Vector poly2Velocity = body2.getVelocity();

        // The translation vectors for both bodies will be 0 when there is no intersection
        body1TransVector.setXY(0, 0);
        body2TransVector.setXY(0, 0);

        // Determining if the bodies intersect or not
        if (isIntersecting(poly1Vertices, poly2Vertices, mtd)) {

            // If the two objects are not touching, they are not colliding!
            if (mtd.getX() == 0 && mtd.getY() == 0) {
                return false;
            }

            Vector body1Trans = getTranslationVectors(mtd, poly1CenterPt, poly2CenterPt, poly1Velocity, poly2Velocity);
            Vector body2Trans = getTranslationVectors(mtd, poly2CenterPt, poly1CenterPt, poly2Velocity, poly1Velocity);

            body1TransVector.setXY(body1Trans.getX(), body1Trans.getY());
            body2TransVector.setXY(body2Trans.getX(), body2Trans.getY());

            return true;
        }
        return false;
    }
}