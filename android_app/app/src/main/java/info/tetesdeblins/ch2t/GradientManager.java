package info.tetesdeblins.ch2t;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import java.util.Random;


public class GradientManager {
    private Random mRandom = new Random();
    private Context mContext;
    private Point mSize;

    public GradientManager(Context context, Point size){
        this.mContext = context;
        this.mSize = size;
    }

    // Custom method to generate my LinearGradient
    protected LinearGradient getMyLinearGradient(){
        /*
            public LinearGradient (float x0, float y0, float x1, float y1, int[] colors, float[]
                positions, Shader.TileMode tile)

                Create a shader that draws a linear gradient along a line.

                Parameters
                x0 : The x-coordinate for the start of the gradient line
                y0 : The y-coordinate for the start of the gradient line
                x1 : The x-coordinate for the end of the gradient line
                y1 : The y-coordinate for the end of the gradient line
                colors : The colors to be distributed along the gradient line
                positions : May be null. The relative positions [0..1] of each corresponding color
                    in the colors array. If this is null, the the colors are distributed evenly
                    along the gradient line.
                tile : The Shader tiling mode
        */
        int[] colors = {
            Color.HSVToColor(255, new float[]{10, (float) 8.7, (float) 8.7}),
            Color.HSVToColor(255, new float[]{53, (float) 9.1, (float) 9.7})
        };

        LinearGradient gradient = new LinearGradient(
                0,
                0,
                mSize.x,
                mSize.y,
                colors, // Colors to draw the gradient
                null, // No position defined
                Shader.TileMode.CLAMP // Shader tiling mode
        );
        // Return the LinearGradient
        return gradient;
    }

    // Custom method to generate random Shader TileMode
    protected Shader.TileMode getRandomShaderTileMode(){
        /*
            Shader
                Shader is the based class for objects that return horizontal spans of colors during
                drawing. A subclass of Shader is installed in a Paint calling paint.setShader(shader).
                After that any object (other than a bitmap) that is drawn with that paint will get
                its color(s) from the shader.
        */
        Shader.TileMode mode;
        int indicator = mRandom.nextInt(3);
        if(indicator==0){
            /*
                Shader.TileMode : CLAMP
                    replicate the edge color if the shader draws outside of its original bounds
            */
            mode = Shader.TileMode.CLAMP;
        }else if(indicator==1){
            /*
                Shader.TileMode : MIRROR
                    repeat the shader's image horizontally and vertically, alternating mirror images
                    so that adjacent images always seam
            */
            mode = Shader.TileMode.MIRROR;
        }else {
            /*
                Shader.TileMode : REPEAT
                    repeat the shader's image horizontally and vertically
            */
            mode = Shader.TileMode.REPEAT;
        }
        // Return the random Shader TileMode
        return mode;
    }

    // Custom method to generate random color array
    protected int[] getRandomColorArray(){
        int length = mRandom.nextInt(16-3)+3;
        int[] colors = new int[length];
        for (int i=0; i<length;i++){
            colors[i]=getRandomHSVColor();
        }
        // Return the color array
        return colors;
    }

    // Custom method to generate random HSV color
    protected int getRandomHSVColor(){
        /*
            Hue is the variation of color
            Hue range 0 to 360

            Saturation is the depth of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% solid color

            Value/Black is the lightness of color
            Range is 0.0 to 1.0 float value
            1.0 is 100% bright less of a color that means black
        */

        // Generate a random hue value between 0 to 360
        int hue = mRandom.nextInt(361);

        // We make the color depth full
        float saturation = 1.0f;

        // We make a full bright color
        float value = 1.0f;

        // We avoid color transparency
        int alpha = 255;

        // Finally, generate the color
        int color = Color.HSVToColor(alpha,new float[]{hue,saturation,value});

        // Return the color
        return color;
    }
}