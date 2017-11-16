package com.example.alexanderibsen.spookbusters.Objects;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

import static java.lang.Math.abs;
import static java.lang.Math.random;

/**
 * Created by marti on 15/11/2017.
 */

public class Ghost3D extends Object3D {

    private SimpleVector position = new SimpleVector();
    private SimpleVector velocity = new SimpleVector();
    private float retreatDistance=15;
    private float timer = (float) (random()*1000);

    enum GhostBehaviour {
        APPROACH,
        RETREAT,
        CIRCLE_LEFT,
        CIRCLE_RIGHT,
        VANISH
    }

    GhostBehaviour currentBehaviour = GhostBehaviour.APPROACH;

    public Ghost3D(Object3D obj) {
        super(obj);
    }

    public void moveTo(float x, float y, float z){
        position.set(x, y, z);
        clearTranslation();
        translate(x, y, z);
        lookAt(SimpleVector.ORIGIN);
    }

    public void setRotateY(float rotY){
        clearRotation();
        clearTranslation();
        rotateY(rotY);
        translate(position);
    }

    public void lookAt(SimpleVector lookTarget){
        SimpleVector lookDirection = lookTarget.calcSub(position).normalize();
        float angle = (float) (Math.PI/2+(float) Math.atan2(lookDirection.z, lookDirection.x));
        //setRotationPivot(position);
        //Log.e("spookbuster.Ghost3d", position.toString());
        //Log.e("spookbuster.Ghost3d", lookTarget.toString());
        if(abs(angle)>0.01)setRotateY(-angle);
    }

    public SimpleVector getPosition() {
        return position;
    }

    public void update(long deltaTime) {
        timer += deltaTime;
        SimpleVector targetVelocity = new SimpleVector();
        SimpleVector normalize = position.normalize();
        switch (currentBehaviour){
            case APPROACH:
                targetVelocity.sub(normalize);

                if(position.length() < 1) currentBehaviour = GhostBehaviour.RETREAT;
                break;
            case RETREAT:
                if(position.length() > retreatDistance){
                    float dice = (float) (random()*100);
                    if(dice < 45){
                        currentBehaviour = GhostBehaviour.CIRCLE_LEFT;
                    } else if(dice > 55){
                        currentBehaviour = GhostBehaviour.CIRCLE_RIGHT;
                    } else {
                        currentBehaviour = GhostBehaviour.APPROACH;
                    }
                } else {
                    targetVelocity.add(normalize);
                }
                break;
            case CIRCLE_LEFT:
                normalize.rotateY((float) (Math.PI/2));
                targetVelocity.add(normalize);
                if(random()*1000 < 1){
                    currentBehaviour = GhostBehaviour.APPROACH;
                }
                break;
            case CIRCLE_RIGHT:
                normalize.rotateY((float) (-Math.PI/2));
                targetVelocity.add(normalize);
                if(random() * 1000 < 1){
                    currentBehaviour = GhostBehaviour.APPROACH;
                }
                break;
            case VANISH:
                targetVelocity.add(new SimpleVector(0,-1,0));
                break;
        }
        SimpleVector simpleVector = new SimpleVector(velocity);
        simpleVector.scalarMul(deltaTime/1000f);
        position.y = (float) (Math.sin(timer/2000*Math.PI));
        moveTo(position.calcAdd(simpleVector));
        velocity.set(lerpVector(velocity, targetVelocity, 0.1f));
    }

    SimpleVector lerpVector(SimpleVector from, SimpleVector to, float t){
        SimpleVector result = new SimpleVector(from.x*(1-t)+to.x*t, from.y*(1-t)+to.y*t, from.z*(1-t)+to.z*t);
        return result;
    }

    private void moveTo(SimpleVector vector) {
        moveTo(vector.x, vector.y, vector.z);
    }
}
