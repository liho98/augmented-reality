package augmentedReality;

import android.net.Uri;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ar.R;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
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
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class CustomArFragment extends ArFragment {

    private ArFragment arFragment;
    private Session session;
    private Frame frame;
    private Pose pose;
    private Anchor anchor;
    private AnchorNode anchorNode;
    private Node node;

    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;

    @Override
    public void onStart() {
        super.onStart();

        arFragment = (ArFragment) getFragmentManager().findFragmentById(R.id.fragment);
        hidePlaneInstructionView();

        ViewRenderable.builder()
                .setView(this.getActivity(), R.layout.card)
                .build()
                .thenAccept(renderable -> viewRenderable = renderable);




/*        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (viewRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(viewRenderable);
                    andy.select();
                });*/

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onSceneUpdate);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
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

        if (arFragment.getArSceneView().getScene() == null) {
            return;
        }

        // Place the anchor 1m in front of the camera if anchorNode is null.
        if (this.node == null) {
            setAnchorNode();

        }else{
            Vector3 cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 cardPosition = node.getWorldPosition();
            Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            node.setWorldRotation(lookRotation);
        }

    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        return new Config(session);
    }

    private void hidePlaneInstructionView() {
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);
    }

    private void nodeAlwaysFaceCamera() {
        Vector3 cameraPosition = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = node.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        node.setWorldRotation(lookRotation);
    }

    private void setNode() {
        node = new Node();
        node.setParent(arFragment.getArSceneView().getScene());
        node.setWorldPosition(new Vector3(0f, 0f, -2));
    }

    private void setAnchorNode() {
/*        Session session = arFragment.getArSceneView().getSession();
        float[] pos = { 0,0,-2 };
        float[] rotation = {0,0,0,1};
        Anchor anchor =  session.createAnchor(new Pose(pos, rotation));
        anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(viewRenderable);
        anchorNode.setParent(arFragment.getArSceneView().getScene());*/

        // Find a position half a meter in front of the user.
        Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
        Vector3 position = Vector3.add(cameraPos, cameraForward.scaled(2f));

        // Create an ARCore Anchor at the position.
        Pose pose = Pose.makeTranslation(2f, 0f, -position.z);
        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

        // Create the Sceneform AnchorNode
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create the node relative to the AnchorNode
        node = new Node();
        node.setParent(anchorNode);
        node.setRenderable(viewRenderable);
    }

    private void viewRenderable() {

        ViewRenderable.builder()
                .setView(this.getActivity(), R.layout.card)
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
                .setSource(this.getActivity(), Uri.parse("model.sfb"))
                .build()
                .thenAccept(renderable -> modelRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast = Toast.makeText(this.getActivity(), "Unable to load arrow renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

}
