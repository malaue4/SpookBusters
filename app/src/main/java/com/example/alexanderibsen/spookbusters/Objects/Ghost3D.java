package com.example.alexanderibsen.spookbusters.Objects;

import android.util.Log;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

import static java.lang.Math.abs;

/**
 * Created by marti on 15/11/2017.
 */

public class Ghost3D extends Object3D {

    SimpleVector position = new SimpleVector();

    public Ghost3D(Object3D obj) {
        super(obj);
    }

    public void moveTo(float x, float y, float z){
        position.set(x, y, z);
        clearTranslation();
        translate(x, y, z);
        //lookAt(SimpleVector.ORIGIN);
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
}
