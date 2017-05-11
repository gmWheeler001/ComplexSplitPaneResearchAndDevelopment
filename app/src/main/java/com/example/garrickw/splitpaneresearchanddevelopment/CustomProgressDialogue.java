package com.example.garrickw.splitpaneresearchanddevelopment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;

import com.eftimoff.androipathview.PathView;


/**
 * Created by garrick.w on 2015/10/20.
 * This class draws the diagram and sets up its animations
 */
public class CustomProgressDialogue {
    private Activity thisActivity;
    private RelativeLayout diagramContainerView;
    private PathView diagramPathView[], diagramPathViewReverse[];
    private int lineColor = Color.parseColor("#f1f1f1");
    private double lineThickness = 4;
    private int delayTimeBetweenAnimations = 300;
    private double speed = 1;
    private boolean runAnimations = true;
    private final boolean[] activeAnimations = new boolean[4];

    public CustomProgressDialogue(Activity activity, RelativeLayout holderView) {
        thisActivity = activity;
        diagramContainerView = holderView;
    }

    public CustomProgressDialogue setLineColor(int color, int thickness) {
        lineColor = color;
        lineThickness = thickness;
        return this;
    }

    public CustomProgressDialogue setSpeed(double speedInput) {
        this.speed = speedInput;
        return this;
    }

    public CustomProgressDialogue setDelay(int delayInput) {
        this.delayTimeBetweenAnimations = delayInput;
        return this;
    }

    public CustomProgressDialogue show() {
        runAnimations = true;
        diagramContainerView.removeAllViews();

        drawK2Logo(110, 5);
        beginAnimationWithDelay(0, diagramPathView[0], diagramPathViewReverse[0], (int) (1200 / speed), 0);
        beginAnimationWithDelay(delayTimeBetweenAnimations, diagramPathView[1], diagramPathViewReverse[1], (int) (1200 / speed), 1);
        beginAnimationWithDelay(delayTimeBetweenAnimations * 3, diagramPathView[2], diagramPathViewReverse[2], (int) (1200 / speed), 2);
        beginAnimationWithDelay(delayTimeBetweenAnimations * 2, diagramPathView[3], diagramPathViewReverse[3], (int) (1200 / speed), 3);
        return this;
    }

    public void dismiss() {
        runAnimations = false;
    }

    private void beginAnimationWithDelay(final int delay, final PathView forwardPathView,
                                         final PathView backwardPathView, final int speed, final int animationNumber) {
        final boolean[] delayRun = {false};
        activeAnimations[animationNumber] = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (runAnimations) {
                    try {
                        if (!delayRun[0]) {
                            delayRun[0] = true;
                            Thread.sleep(delay);
                        }

                        if (runAnimations) {
                            createAnimation(forwardPathView, 0, speed, true);
                            Thread.sleep(speed);

                            createAnimation(backwardPathView, 0, speed, false);
                            Thread.sleep(speed);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                activeAnimations[animationNumber] = false;

                thisActivity.runOnUiThread(new Runnable() {
                                               @Override
                                               public void run() {
                                                   diagramContainerView.removeView(forwardPathView);
                                                   diagramContainerView.removeView(backwardPathView);

                                                   boolean closeWindow = true;
                                                   for (boolean activeAnimation : activeAnimations) {
                                                       if (activeAnimation) {
                                                           closeWindow = false;
                                                       }
                                                   }

                                                   if (closeWindow) {
                                                       Animation fadeOut = new AlphaAnimation(1, 0);
                                                       fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                                                       fadeOut.setDuration(300);

                                                       fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                                           @Override
                                                           public void onAnimationStart(Animation animation) {

                                                           }

                                                           @Override
                                                           public void onAnimationEnd(Animation animation) {
                                                               thisActivity.findViewById(R.id.loadingWindowContainerViewRelativeLayout).setVisibility(View.GONE);
                                                           }

                                                           @Override
                                                           public void onAnimationRepeat(Animation animation) {

                                                           }
                                                       });

                                                       thisActivity.findViewById(R.id.loadingWindowContainerViewRelativeLayout).startAnimation(fadeOut);

                                                   }
                                               }
                                           }

                );
            }
        }).start();
    }

    private CustomProgressDialogue drawK2Logo(int size, int padding) {

        // create & init the diagrams
        diagramPathView = new PathView[4];
        diagramPathViewReverse = new PathView[4];

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(size + (padding * 2),
                size + (padding * 2));
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        for (int i = 0; i < diagramPathView.length; i++) {
            diagramPathView[i] = new PathView(thisActivity);
            diagramPathView[i].setPathColor(lineColor);
            diagramPathView[i].setPathWidth((int) lineThickness);

            diagramPathViewReverse[i] = new PathView(thisActivity);
            diagramPathViewReverse[i].setPathColor(lineColor);
            diagramPathViewReverse[i].setPathWidth((int) lineThickness);

            diagramContainerView.addView(diagramPathView[i], rlp);
            diagramContainerView.addView(diagramPathViewReverse[i], rlp);
        }

        // first cube
        diagramPathView[0].setPath(drawCubeForK2Logo(size, 5, (size / 11) + 5, true));
        diagramPathViewReverse[0].setPath(drawCubeForK2Logo(size, 5, (size / 11) + 5, false));

        // second cube
        diagramPathView[1].setPath(drawCubeForK2Logo(size, ((13 * size) / 22) + 4, 6, true));
        diagramPathViewReverse[1].setPath(drawCubeForK2Logo(size, ((13 * size) / 22) + 4, 6, false));

        // third cube
        diagramPathView[2].setPath(drawCubeForK2Logo(size, 5, (size * 7) / 11, true));
        diagramPathViewReverse[2].setPath(drawCubeForK2Logo(size, 5, (size * 7) / 11, false));

        // fourth cube
        diagramPathView[3].setPath(drawCubeForK2Logo(size, (size * 25) / 44, (size * 7) / 11, true));
        diagramPathViewReverse[3].setPath(drawCubeForK2Logo(size, (size * 25) / 44, (size * 7) / 11, false));

        return this;
    }

    private Path drawCubeForK2Logo(final double width, final double xOffset, final double yOffset, boolean forward) {
        Path returnPath = new Path();
        // get the size of the path to draw
        double curveOffset = (width / 11) / 2;

        double rightX = xOffset + ((width * 5) / 11) - curveOffset - (lineThickness / 2);
        double lineLength = (rightX - curveOffset - (lineThickness / 2)) - xOffset + curveOffset;
        double bottomY = yOffset + lineLength + (lineThickness / 2);

        // we have 8 points to cover, here we calculate them all
        Point topLineLeft = new Point((int) (xOffset + curveOffset), (int) (yOffset + (lineThickness / 2)));
        Point topLineRight = new Point((int) (xOffset + lineLength), topLineLeft.y);
        Point rightLineTop = new Point((int) (rightX), (int) (yOffset + curveOffset));
        Point rightLineBottom = new Point(rightLineTop.x, (int) (bottomY - curveOffset));
        Point bottomLineRight = new Point(topLineRight.x, (int) (bottomY));
        Point bottomLineLeft = new Point(topLineLeft.x, bottomLineRight.y);
        Point leftLineBottom = new Point((int) (xOffset + (lineThickness / 2)), rightLineBottom.y);
        Point leftLineTop = new Point(leftLineBottom.x, rightLineTop.y);

        // we have fourCorners
        Point topRightCorner = new Point(rightLineTop.x, topLineLeft.y);
        Point bottomRightCorner = new Point(rightLineTop.x, bottomLineRight.y);
        Point bottomLeftCorner = new Point(leftLineTop.x, bottomLineRight.y);
        Point topLeftCorner = new Point(leftLineTop.x, topLineLeft.y);

        // starting point
        returnPath.moveTo(topLineLeft.x, topLineLeft.y);

        if (forward) {
            returnPath.lineTo(topLineRight.x, topLineRight.y);
            returnPath.quadTo(topRightCorner.x, topRightCorner.y, rightLineTop.x, rightLineTop.y);
            returnPath.lineTo(rightLineBottom.x, rightLineBottom.y);
            returnPath.quadTo(bottomRightCorner.x, bottomRightCorner.y, bottomLineRight.x, bottomLineRight.y);
            returnPath.lineTo(bottomLineLeft.x, bottomLineLeft.y);
            returnPath.quadTo(bottomLeftCorner.x, bottomLeftCorner.y, leftLineBottom.x, leftLineBottom.y);
            returnPath.lineTo(leftLineTop.x, leftLineTop.y);
            returnPath.quadTo(topLeftCorner.x, topLeftCorner.y, topLineLeft.x, topLineLeft.y);
        } else {
            returnPath.quadTo(topLeftCorner.x, topLeftCorner.y, leftLineTop.x, leftLineTop.y);
            returnPath.lineTo(leftLineBottom.x, leftLineBottom.y);
            returnPath.quadTo(bottomLeftCorner.x, bottomLeftCorner.y, bottomLineLeft.x, bottomLineLeft.y);
            returnPath.lineTo(bottomLineRight.x, bottomLineRight.y);
            returnPath.quadTo(bottomRightCorner.x, bottomRightCorner.y, rightLineBottom.x, rightLineBottom.y);
            returnPath.lineTo(rightLineTop.x, rightLineTop.y);
            returnPath.quadTo(topRightCorner.x, topRightCorner.y, topLineRight.x, topLineRight.y);
        }
        returnPath.close();

        return returnPath;
    }

    private void createAnimation(final PathView pathViewInput, final int delay, final int duration, final boolean forward) {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pathViewInput.getPathAnimator()
                        .delay(delay)
                        .duration(duration)
                        .interpolator(new Interpolator() {
                            @Override
                            public float getInterpolation(float v) {
                                if (forward) return v;
                                else return 1 - v;
                            }
                        })
                        .listenerEnd(new PathView.AnimatorBuilder.ListenerEnd() {
                            @Override
                            public void onAnimationEnd() {
                                pathViewInput.setPercentage(0);
                            }
                        })
                        .start();
            }
        });
    }

}
