package com.example.ar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class CustomArFragment extends ArFragment {

    private ArFragment arFragment;
    private Session session;
    private Frame frame;
    private Anchor anchor;
    private Pose pose;
    private Node node;
    private AnchorNode anchorNode;

    private ViewRenderable viewRenderable;
    private ModelRenderable modelRenderable;


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);
        arFragment = (ArFragment) getFragmentManager().findFragmentById(R.id.fragment);



        node = new Node();
        node.setParent(arFragment.getArSceneView().getScene());
        node.setWorldPosition(new Vector3(0f, 0f, -2f));
        ViewRenderable.builder()
                .setView(this.getActivity(), R.layout.card)
                .build()
                .thenAccept(renderable -> viewRenderable = renderable);
        node.setRenderable(viewRenderable);
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        return new Config(session);
    }

}
