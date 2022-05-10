package magnus;

import com.jme3.app.SimpleApplication;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.control.VehicleControl;
import com.jme3.bullet.objects.VehicleWheel;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Main extends SimpleApplication implements ActionListener {

    private BulletAppState bulletAppState;
    private VehicleControl bot;
    private VehicleWheel fr, fl, ml, mr, br, bl;
    private Node node_fr, node_fl, node_ml, node_mr, node_br, node_bl;
    private float wheelRadius;
    private float steeringValue = 0;
    private float accelerationValue = 0;
    private float direction = 1;
    private Node magnusNode;
    private Node fieldNode;
    
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    private void setupKeys() {
        inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_H));
        inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_U));
        inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, "Lefts");
        inputManager.addListener(this, "Rights");
        inputManager.addListener(this, "Ups");
        inputManager.addListener(this, "Downs");
        inputManager.addListener(this, "Space");
        inputManager.addListener(this, "Reset");
    }
    
    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        cam.setFrustumFar(150f);
        flyCam.setMoveSpeed(10);

        setupKeys();
        PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
        buildPlayer();

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
        rootNode.addLight(sun);

        sun = new DirectionalLight();
        sun.setDirection(new Vector3f(0.5f, -1f, 0.3f).normalizeLocal());
    }
    
    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
    
    private Geometry findGeom(Spatial spatial, String name) {
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (int i = 0; i < node.getQuantity(); i++) {
                Spatial child = node.getChild(i);
                Geometry result = findGeom(child, name);
                if (result != null) {
                    return result;
                }
            }
        } else if (spatial instanceof Geometry) {
            if (spatial.getName().startsWith(name)) {
                return (Geometry) spatial;
            }
        }
        return null;
    }
    
    private void buildPlayer() {
        Material mat = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.setColor("Color", ColorRGBA.Red);
        
        float stiffness = 120.0f;
        float compValue = 0.2f; 
        float dampValue = 0.3f;
        final float mass = 200;
        
        Node f = (Node)assetManager.loadModel("Models/rapid_react_field.obj");
        Geometry field = findGeom(f, "rapid_react_field");
        CollisionShape fieldFrame = CollisionShapeFactory.createDynamicMeshShape(field);
        //field.move(0f, -4.75f, 0f);
        //rootNode.attachChild(field);
        fieldNode = PhysicsTestHelper.createPhysicsTestNode(assetManager, fieldFrame, 100);
        fieldNode.getControl(RigidBodyControl.class).setPhysicsLocation(new Vector3f(0f, -2, 0f));
        fieldNode.getControl(RigidBodyControl.class).setApplyPhysicsLocal(true);
        
        magnusNode = (Node)assetManager.loadModel("Models/Robot_FullAssembly2.obj");
        magnusNode.setShadowMode(ShadowMode.Cast);
        Geometry frame = findGeom(magnusNode, "Robot_FullAssembly2");
        frame.scale(4f, 0f, 0f);
        BoundingBox box = (BoundingBox) frame.getModelBound();
        
        CollisionShape magnusHull = CollisionShapeFactory.createDynamicMeshShape(frame);
        
        bot = new VehicleControl(magnusHull, mass);
        magnusNode.addControl(bot);
        
        bot.setSuspensionCompression(compValue * 2.0f * FastMath.sqrt(stiffness));
        bot.setSuspensionDamping(dampValue * 2.0f * FastMath.sqrt(stiffness));
        bot.setSuspensionStiffness(stiffness);
        bot.setMaxSuspensionForce(10000);
        
        //Create four wheels and add them at their locations
        Vector3f wheelDirection = new Vector3f(0, -1, 0); // was 0, -1, 0
        Vector3f wheelAxle = new Vector3f(-1, 0, 0); // was -1, 0, 0
        float radius = 0.125f;
        float restLength = 0.3f;
        float yOff = 0.32f;
        float xOff = 0.30f;
        float zOff = 0.15f;
        
        Node node1 = new Node("wheel 1 node");
        Geometry wheels1 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node1.attachChild(wheels1);
        bot.addWheel(node1, new Vector3f(-xOff, yOff, 0.12f),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node2 = new Node("wheel 2 node");
        Geometry wheels2 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node2.attachChild(wheels2);
        bot.addWheel(node2, new Vector3f(xOff, yOff, 0.12f),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node3 = new Node("wheel 3 node");
        Geometry wheels3 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node3.attachChild(wheels3);
        bot.addWheel(node3, new Vector3f(-xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node4 = new Node("wheel 4 node");
        Geometry wheels4 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node4.attachChild(wheels4);
        bot.addWheel(node4, new Vector3f(xOff, yOff, -zOff),
                wheelDirection, wheelAxle, restLength, radius, false);
        
        Node node5 = new Node("wheel 5 node");
        Geometry wheels5 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node5.attachChild(wheels5);
        bot.addWheel(node5, new Vector3f(-xOff, yOff, -0.43f),
                wheelDirection, wheelAxle, restLength, radius, false);

        Node node6 = new Node("wheel 6 node");
        Geometry wheels6 = (Geometry)assetManager.loadModel("Models/colson_wheels.obj");
        node6.attachChild(wheels6);
        bot.addWheel(node6, new Vector3f(xOff, yOff, -0.43f),
                wheelDirection, wheelAxle, restLength, radius, false);

        bot.getWheel(2).setFrictionSlip(4);
        bot.getWheel(3).setFrictionSlip(4);
        
        magnusNode.attachChild(node1);
        magnusNode.attachChild(node2);
        magnusNode.attachChild(node3);
        magnusNode.attachChild(node4);
        magnusNode.attachChild(node5);
        magnusNode.attachChild(node6);
        rootNode.attachChild(magnusNode);
        rootNode.attachChild(fieldNode);
        
        getPhysicsSpace().add(fieldNode);
        getPhysicsSpace().add(bot);
    }
    
    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            if (value) {
                accelerationValue += (85 * direction);
            } else {
                accelerationValue -= (85 * direction);
                bot.brake(40f);
            }
            bot.accelerate(0, accelerationValue);
            bot.accelerate(2, accelerationValue);
            bot.accelerate(4, accelerationValue);
            bot.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(magnusNode, "Robot_FullAssembly2")));
        } else if (binding.equals("Rights")) {
            if (value) {
                accelerationValue += (85 * direction);
            } else {
                accelerationValue -= (85 * direction);
                bot.brake(40f);
            }
            bot.accelerate(1, accelerationValue);
            bot.accelerate(3, accelerationValue);
            bot.accelerate(5, accelerationValue);
            bot.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(magnusNode, "Robot_FullAssembly2")));
        } 
        else if (binding.equals("Ups")) {
            if (value) {
                accelerationValue += 50;
            } else {
                accelerationValue -= 50;
                bot.brake(40f);
            }
            direction = 1;
            bot.accelerate(accelerationValue);
            bot.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(magnusNode, "Robot_FullAssembly2")));
        } else if (binding.equals("Downs")) {
            if (value) {
                accelerationValue -= 50;
            } else {
                accelerationValue += 50;
                bot.brake(40f);
            }
            direction = -1;
            bot.accelerate(accelerationValue);
            bot.setCollisionShape(CollisionShapeFactory.createDynamicMeshShape(findGeom(magnusNode, "Robot_FullAssembly2")));
        } else if (binding.equals("Reset")) {
            if (value) {
                System.out.println("Reset");
                bot.setPhysicsLocation(Vector3f.ZERO);
                bot.setPhysicsRotation(new Matrix3f());
                bot.setLinearVelocity(Vector3f.ZERO);
                bot.setAngularVelocity(Vector3f.ZERO);
                bot.resetSuspension();
            } else {
            }
        }
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        cam.lookAt(magnusNode.getWorldTranslation(), Vector3f.UNIT_Y);
    }
}