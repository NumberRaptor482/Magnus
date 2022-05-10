/*
 * Copyright (c) 2009-2019 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package magnus;

import com.jme3.app.Application;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Plane;
import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;

/**
 *
 * @author normenhansen
 */
public class PhysicsTestHelper {

    /**
     * creates a simple physics test world with a floor, an obstacle and some
     * test boxes
     *
     * @param rootNode where lights and geometries should be added
     * @param assetManager for loading assets
     * @param space where collision objects should be added
     */
    public static void createPhysicsTestWorld(Node rootNode, AssetManager assetManager, PhysicsSpace space) {
        AmbientLight light = new AmbientLight();
        light.setColor(ColorRGBA.LightGray);
        rootNode.addLight(light);

        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/field.png"));

        Material blue = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        blue.setTexture("ColorMap", assetManager.loadTexture("Interface/blue.png"));
        
        Material red = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        red.setTexture("ColorMap", assetManager.loadTexture("Interface/red.png"));
        
        Box floorBox = new Box(10, 0.25f, 17);
        Geometry floorGeometry = new Geometry("Floor", floorBox);
        floorGeometry.setMaterial(material);
        floorGeometry.setLocalTranslation(0, -2, 0);
        Plane plane = new Plane();
        plane.setOriginNormal(new Vector3f(0, 0.25f, 0), Vector3f.UNIT_Y);
        floorGeometry.addControl(new RigidBodyControl(new PlaneCollisionShape(plane), 0));
        floorGeometry.addControl(new RigidBodyControl(0));
        rootNode.attachChild(floorGeometry);
        space.add(floorGeometry);

        //
        for (int i = 0; i < 6; i++) {
            Sphere ball = new Sphere(20, 20, 0.125f);
            Geometry ballGeometry = new Geometry("Ball", ball);
            ballGeometry.setMaterial(blue);
            ballGeometry.setLocalTranslation(i, 5, -3);
            ballGeometry.addControl(new RigidBodyControl(2));
            rootNode.attachChild(ballGeometry);
            space.add(ballGeometry);
        }
        for (int i = 0; i < 6; i++) {
            Sphere ball = new Sphere(20, 20, 0.125f);
            Geometry ballGeometry = new Geometry("Ball", ball);
            ballGeometry.setMaterial(red);
            ballGeometry.setLocalTranslation(-i, 5, -3);
            ballGeometry.addControl(new RigidBodyControl(2));
            rootNode.attachChild(ballGeometry);
            space.add(ballGeometry);
        }
    }

    /**
     * creates a box geometry with a RigidBodyControl
     *
     * @param assetManager for loading assets
     * @return a new Geometry
     */
    public static Geometry createPhysicsTestBox(AssetManager assetManager) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/rapidreact_logo.png"));
        Box box = new Box(0.25f, 0.25f, 0.25f);
        Geometry boxGeometry = new Geometry("Box", box);
        boxGeometry.setMaterial(material);
        //RigidBodyControl automatically uses box collision shapes when attached to single geometry with box mesh
        boxGeometry.addControl(new RigidBodyControl(2));
        return boxGeometry;
    }

    /**
     * creates a sphere geometry with a RigidBodyControl
     *
     * @param assetManager for loading assets
     * @return a new Geometry
     */
    public static Geometry createPhysicsTestSphere(AssetManager assetManager) {
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setTexture("ColorMap", assetManager.loadTexture("Interface/rapidreact_logo.png"));
        Sphere sphere = new Sphere(8, 8, 0.25f);
        Geometry boxGeometry = new Geometry("Sphere", sphere);
        boxGeometry.setMaterial(material);
        //RigidBodyControl automatically uses sphere collision shapes when attached to single geometry with sphere mesh
        boxGeometry.addControl(new RigidBodyControl(2));
        return boxGeometry;
    }

    /**
     * creates an empty node with a RigidBodyControl
     *
     * @param manager for loading assets
     * @param shape a shape for the collision object
     * @param mass a mass for rigid body
     * @return a new Node
     */
    public static Node createPhysicsTestNode(AssetManager manager, CollisionShape shape, float mass) {
        Node node = new Node("PhysicsNode");
        RigidBodyControl control = new RigidBodyControl(shape, mass);
        node.addControl(control);
        return node;
    }

}