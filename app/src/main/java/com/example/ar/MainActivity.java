package com.example.ar;

import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Iterator;
import java.util.List;

import helper.LocationHelper;
import helper.MathHelper;
import helper.SensorHelper;

public class MainActivity extends AppCompatActivity {

    //AR
    private CustomArFragment arFragment;
    private Session session;
    private Frame frame;
    private Anchor anchor;
    private Pose pose;
    private Node node;
    private AnchorNode anchorNode;

    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;

    //Helper
    private LocationHelper locationHelper;
    private SensorHelper sensorHelper;
    private MathHelper mathHelper;

    private float bearing;

    private boolean renderred = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        sensorHelper = new SensorHelper(this);
        locationHelper = new LocationHelper(this);
        locationHelper.onCreate();
        mathHelper = new MathHelper();

        hidePlaneInstructionView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);

    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorHelper.onResume();
        locationHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        sensorHelper.onPause();
        locationHelper.onPause();
    }

    private void onSceneUpdate(FrameTime frameTime) {
        // Let the fragment update its state first.
        arFragment.onUpdate(frameTime);

        // If there is no frame then don't process anything.
        if (arFragment.getArSceneView().getArFrame() == null) {
            return;
        }

        // If ARCore is not tracking yet, then don't process anything.
        if (arFragment.getArSceneView().getArFrame().getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return;
        }

        if (arFragment.getArSceneView().getScene() == null) {
            return;
        }

        if (locationHelper.isHasLocation()) {
            setTextView1();

            bearing = mathHelper.bearingBtw2Points(locationHelper.getLocation().getLatitude(), locationHelper.getLocation().getLongitude(), 3.2165235, 101.7253802);
            //setTextView2();

            Location targetLocation = new Location("Block k");//provider name is unnecessary
            targetLocation.setLatitude(3.2169224);//your coords of course
            targetLocation.setLongitude(101.7252331);


            TextView textView = (TextView) findViewById(R.id.text_view_liho2);
            textView.setText(String.format("%f \n%f", sensorHelper.getHeading(), bearing));//968.62

            //setNode();
            //viewRenderable();
            //checkPlane();

        }

    }

    private void setTextView1() {
        TextView textView = (TextView) findViewById(R.id.text_view_liho);
        textView.setText(String.format("%f \n%f", locationHelper.getLocation().getLatitude(), locationHelper.getLocation().getLongitude()));
    }

    private void setTextView2() {
        TextView textView = (TextView) findViewById(R.id.text_view_liho2);
        textView.setText(String.format("%f \n%f", sensorHelper.getHeading(), bearing));
    }

    private void setTextView3(float x, float y) {
        TextView textView = (TextView) findViewById(R.id.text_view_liho);
        textView.setText(String.format("%f \n%f", x, y));
    }

    private void setNode() {
        node = new Node();
        node.setParent(arFragment.getArSceneView().getScene());
        node.setWorldPosition(new Vector3(0f, 0f, 0f));
    }

    private void nodeAlwaysFaceCamera() {
        Vector3 cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = node.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        node.setWorldRotation(lookRotation);
    }

    private void viewRenderable() {

        ViewRenderable.builder()
                .setView(this, R.layout.card)
                .build()
                .thenAccept(renderable -> {
                    node.setRenderable(renderable);
                    TextView textView = (TextView) renderable.getView();
                    textView.setText("Block K");
                });

/*        ViewRenderable.builder()
                .setView(this, R.layout.card)
                .build()
                .thenAccept(renderable -> viewRenderable = renderable);*/
    }

    private void modelRenderable() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this, "Unable to load arrow renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    private void hidePlaneInstructionView() {
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
    }

    private void checkPlane() {
        if (frame != null) {

            for (Object o : frame.getUpdatedTrackables(Plane.class)) {

                Plane plane = (Plane) o;

                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    arFragment.getPlaneDiscoveryController().hide();

                    Iterator iterableAnchor = frame.getUpdatedAnchors().iterator();

                    if (!iterableAnchor.hasNext()) {
                        makeAr(plane, frame);
                    }
                }
            }
        }
    }

    public void makeAr(Plane plane, Frame frame) {

        for (int k = 0; k < 10; k++) {
            if (bearing >= sensorHelper.getHeading() - 10 && bearing <= sensorHelper.getHeading() + 10) {
                Toast.makeText(this, "walk", Toast.LENGTH_SHORT).show();
                List<HitResult> hitTest = frame.hitTest(screenCenter().x, screenCenter().y);

                Iterator hitTestIterator = hitTest.iterator();

                while (hitTestIterator.hasNext()) {
                    HitResult hitResult = (HitResult) hitTestIterator.next();

                    anchor = null;
                    anchor = plane.createAnchor(hitResult.getHitPose());

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(MainActivity.this.viewRenderable);

                    float x = anchor.getPose().tx();
                    float y = anchor.getPose().compose(Pose.makeTranslation(0f, 0f, 0)).ty();

                    transformableNode.setWorldPosition(new Vector3(x, y, -k));

                }
            }
        }
    }

    private Vector3 screenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Vector3(vw.getWidth() / 2f, vw.getHeight() / 2f, 0f);
    }

    public float bearingBtw2Points(double latitudeSource, double longitudeSource, double latitudeDes, double longitudeDes) {
        //angleNorth(3.216111, 101.733889, 3.2164133, 101.7335478);

        double lat1 = latitudeSource / 180 * Math.PI;
        double lng1 = longitudeSource / 180 * Math.PI;
        double lat2 = latitudeDes / 180 * Math.PI;
        double lng2 = longitudeDes / 180 * Math.PI;

        double y = Math.sin(lng2 - lng1) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);

        double tan2 = Math.atan2(y, x);
        double bearing = tan2 * 180 / Math.PI;
/*        if (bearing < 0) {
            return (bearing + 360.0);
        } else {
            return bearing;
        }*/
        return Double.valueOf(bearing).floatValue();
    }

}

//testing code

/*        if (mLocation != null && renderred == false) {
                frame = arFragment.getArSceneView().getArFrame();

                // Get projection matrix.
                float[] projmtx = new float[16];
                frame.getCamera().getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

                // Get camera matrix and draw.
                float[] viewmtx = new float[16];
                frame.getCamera().getViewMatrix(viewmtx, 0);

                Location targetLocation = new Location("Block k");//provider name is unnecessary
                targetLocation.setLatitude(3.2165235);//your coords of course
                targetLocation.setLongitude(101.7253802);
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(mLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(targetLocation);
                float[] pointInENU = LocationHelper.ECEFtoENU(mLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[16];

                float[] rotatedProjectionMatrix = new float[16];
                arFragment.getArSceneView().getArFrame().getCamera().getPose().getRotationQuaternion(rotatedProjectionMatrix, 0);

                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3] * arFragment.getArSceneView().getWidth());
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3] * arFragment.getArSceneView().getHeight());

                node = new Node();
                node.setParent(arFragment.getArSceneView().getScene());
                node.setWorldPosition(new Vector3(x, 0f, y));
                viewRenderable();
                setTextView3(x, y);

                renderred = true;
                } else {
                if (node != null) {
                nodeAlwaysFaceCamera();
                }
                }*/

/*    Session session = arFragment.getArSceneView().getSession();
        if (session != null && viewRenderable == null) {
                ViewRenderable.builder()
                .setView(this, R.layout.card)
                .build()
                .thenAccept(renderable -> viewRenderable = renderable);

                //Add an Anchor and a renderable in front of the camera

                float[] pos = {0, 0, -1};
                float[] rotation = {0, 0, 0, 1};
                // Create the Anchor.
                Anchor anchor = session.createAnchor(new Pose(pos, rotation));
                anchorNode = new AnchorNode(anchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());
                anchorNode.setRenderable(viewRenderable);

                }*/
