package com.javaphysicsengine.editor.editor;

import com.javaphysicsengine.api.body.PBody;
import com.javaphysicsengine.api.body.PConstraints;
import com.javaphysicsengine.utils.Vector;

import java.awt.*;

import static com.javaphysicsengine.editor.editor.PEditorPanel.MOUSE_STATE_CIRCLE;
import static com.javaphysicsengine.editor.editor.PEditorPanel.MOUSE_STATE_POLYGON;
import static com.javaphysicsengine.editor.editor.PEditorPanel.MOUSE_STATE_SPRING;
import static com.javaphysicsengine.editor.editor.PEditorPanel.MOUSE_STATE_STRING;

public class PEditorRenderer {
    public static final Color BACKGROUND_COLOR = new Color(60, 60, 60);
    public static final Color CURSOR_COLOR = Color.WHITE;
    public static final Color OBJECT_TEXT_COLOR = Color.WHITE;
    public static final Color POBJECT_NAME_TEXT_COLOR = OBJECT_TEXT_COLOR;
    public static final Color SNAP_POINT_RANGE_COLOR = new Color(207, 176, 41);
    public static final Color POLYGON_INPROGRESS_EDGE_COLOR = Color.WHITE;
    public static final Color POLYGON_FINISHED_EDGE_COLOR = Color.BLACK;
    public static final Color CIRCLE_INPROGRESS_EDGE_COLOR = Color.WHITE;

    // Graphic properties showing which parts are visible
    private boolean isBoundingBoxDisplayed = true;
    private boolean isShapeOutlineDisplayed = true;
    private boolean isShapeFillDisplayed = false;
    private boolean isVelocityVectorsDisplayed = false;
    private boolean isAntiAliasingToggled = false;

    private final PEditorStore store;

    public PEditorRenderer(PEditorStore store) {
        this.store = store;
    }

    public boolean isBoundingBoxDisplayed() {
        return isBoundingBoxDisplayed;
    }

    public void setBoundingBoxDisplayed(boolean boundingBoxDisplayed) {
        isBoundingBoxDisplayed = boundingBoxDisplayed;
    }

    public boolean isShapeOutlineDisplayed() {
        return isShapeOutlineDisplayed;
    }

    public void setShapeOutlineDisplayed(boolean shapeOutlineDisplayed) {
        isShapeOutlineDisplayed = shapeOutlineDisplayed;
    }

    public boolean isShapeFillDisplayed() {
        return isShapeFillDisplayed;
    }

    public void setShapeFillDisplayed(boolean shapeFillDisplayed) {
        isShapeFillDisplayed = shapeFillDisplayed;
    }

    public boolean isVelocityVectorsDisplayed() {
        return isVelocityVectorsDisplayed;
    }

    public void setVelocityVectorsDisplayed(boolean velocityVectorsDisplayed) {
        isVelocityVectorsDisplayed = velocityVectorsDisplayed;
    }

    public boolean isAntiAliasingToggled() {
        return isAntiAliasingToggled;
    }

    public void setAntiAliasingToggled(boolean antiAliasingToggled) {
        isAntiAliasingToggled = antiAliasingToggled;
    }

    public PEditorStore getStore() {
        return store;
    }

    public void renderGraphics(Graphics g, int width, int height, int mouseX, int mouseY, boolean isMouseSnappedToPoint, String mouseState) {
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);

        // If antialiasing toggled
        if (isAntiAliasingToggled()) {
            // Set antialiasing (for smoother but slower graphics)
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        // Draw all the user-created bodies
        for (PBody body : store.getCreatedBodies()) {
            if (isBoundingBoxDisplayed)
                body.drawBoundingBox(g, height);

            if (isShapeOutlineDisplayed)
                body.drawOutline(g, height);

            if (isShapeFillDisplayed)
                body.drawFill(g, height);

            // Draw the name of the object
            g.setColor(POBJECT_NAME_TEXT_COLOR);
            g.drawString(body.getName(), (int) body.getCenterPt().getX(), (int) (height - body.getCenterPt().getY()));
        }

        // Draw the constraints
        for (PConstraints constraint : store.getCreatedConstraints())
            constraint.drawConstraints(g, height);

        // Draw the cursor
        g.setColor(CURSOR_COLOR);
        g.drawLine(mouseX, mouseY - 20, mouseX, mouseY + 20);
        g.drawLine(mouseX - 20, mouseY, mouseX + 20, mouseY);

        handleBodyDrawing(g, width, height, mouseX, mouseY, isMouseSnappedToPoint, mouseState);
    }

    private void handleBodyDrawing(Graphics g, int width, int height, int mouseX, int mouseY, boolean isMouseSnappedToPoint, String mouseState) {
        // Draw the point the cursor is snapped to (if it is snapped to)
        if (isMouseSnappedToPoint) {
            // Draw a circle around the point
            g.setColor(SNAP_POINT_RANGE_COLOR);
            g.drawOval(mouseX - 5, mouseY - 5, 10, 10);
        }

        // Drawing the line that will connect the mouse pos to the last vertex of the polygon
        if (mouseState.equals(MOUSE_STATE_POLYGON) && store.getPolyVertices().size() > 0) {
            Vector lastAddedVertex = store.getPolyVertices().get(store.getPolyVertices().size() - 1);
            g.setColor(POLYGON_INPROGRESS_EDGE_COLOR);
            g.drawLine((int) lastAddedVertex.getX(), (int) lastAddedVertex.getY(), mouseX, mouseY);

            // Drawing the going-to-be-drawn polygons
            g.setColor(POLYGON_FINISHED_EDGE_COLOR);
            for (int i = 0; i < store.getPolyVertices().size() - 1; i++) {
                int x1 = (int) store.getPolyVertices().get(i).getX();
                int y1 = (int) store.getPolyVertices().get(i).getY();
                int x2 = (int) store.getPolyVertices().get(i + 1).getX();
                int y2 = (int) store.getPolyVertices().get(i + 1).getY();
                g.drawLine(x1, y1, x2, y2);
            }
        }

        // Drawing the circle that will be drawn
        else if (mouseState.equals(MOUSE_STATE_CIRCLE) && store.getCircleRadius() > 0) {
            int topLeftX = (int) (store.getCircleCenterPt().getX() - store.getCircleRadius());
            int topLeftY = (int) (store.getCircleCenterPt().getY() - store.getCircleRadius());
            g.setColor(CIRCLE_INPROGRESS_EDGE_COLOR);
            g.drawOval(topLeftX, topLeftY, (int) (store.getCircleRadius() * 2), (int) (store.getCircleRadius() * 2));
            g.fillOval((int) store.getCircleCenterPt().getX() - 2, (int) store.getCircleCenterPt().getY() - 2, 4, 4);
        }

        // Drawing the constraint that will be drawn
        else if (mouseState.equals(MOUSE_STATE_SPRING) || mouseState.equals(MOUSE_STATE_STRING))
            if (store.getAttachedBody1() != null) {
                // Draw a line from the centerpt of attachedBody1 to the mouse
                g.setColor(Color.YELLOW);
                g.drawLine((int) store.getAttachedBody1().getCenterPt().getX(), height - (int) store.getAttachedBody1().getCenterPt().getY(), mouseX, mouseY);
            }

    }
}
