package com.example.garrickw.splitpaneresearchanddevelopment.adaptors;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.garrickw.splitpaneresearchanddevelopment.R;
import com.example.garrickw.splitpaneresearchanddevelopment.dummy.TaskModel;

/**
 * Created by garrick.w on 2015/10/16.
 * This Class controls the Task List interface, loading, and behavior
 */
public class TaskListRecycleViewAdaptor extends RecyclerView.Adapter<TaskListRecycleViewAdaptor.ViewHolder> {
    private Callbacks callBacks;

    private TaskModel[] taskData;
    private int selectedIndex = 0;
    private ViewHolder selectedView;

    public TaskListRecycleViewAdaptor(TaskModel[] taskDataInput, TaskListRecycleViewAdaptor.Callbacks cb) {
        this.callBacks = cb;
        this.taskData = taskDataInput;
    }

    public interface Callbacks {
        boolean updateDownPoint(float x, float y);
        boolean movePointer(float x, float y);
        boolean movePointerIgnoreY(float x);
        boolean pointerUp(float x, float y);
        void updateSelection(TaskModel taskModel);
    }

    @Override
    public TaskListRecycleViewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create view and view holder
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_cell, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLayoutView.setLayoutParams(lp);

        return new ViewHolder(itemLayoutView);
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.txtViewTitle.setText(taskData[position].getName());
        viewHolder.txtViewFolio.setText(taskData[position].getFolio());
        viewHolder.txtViewProcess.setText(taskData[position].getProcess());
        viewHolder.position = position;

        if (position == selectedIndex) {
            viewHolder.linearLayoutSelectedIndexIndicator.setBackgroundResource(R.color.menuSelection);
            selectedView = viewHolder;
        } else {
            viewHolder.linearLayoutSelectedIndexIndicator.setBackgroundColor(Color.parseColor("#00000000"));
        }
    }

    // class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtViewTitle, txtViewFolio, txtViewProcess;
        public RelativeLayout linearLayoutSelectedIndexIndicator;
        public LinearLayout linearLayoutSelector;
        public int position;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.task_cell_name);
            txtViewFolio = (TextView) itemLayoutView.findViewById(R.id.task_cell_folio);
            txtViewProcess = (TextView) itemLayoutView.findViewById(R.id.task_cell_process);
            linearLayoutSelectedIndexIndicator = (RelativeLayout) itemLayoutView.findViewById(R.id.selectedIndicator);
            linearLayoutSelector = (LinearLayout) itemLayoutView.findViewById(R.id.linearLayoutSelector);
            final ViewHolder thisViewHolder = this;
            linearLayoutSelector.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        return callBacks.updateDownPoint(event.getRawX(), event.getRawY());

                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        return callBacks.movePointerIgnoreY(event.getRawX());

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        // check to see if the user has swiped before updating the selection
                        if (callBacks.pointerUp(event.getRawX(), event.getRawY())) {
                            return false;
                        }

                        // here we define the action up, as this will tell the app when the user selects something
                        if (selectedView != null) {
                            Integer colorFrom = Color.parseColor("#C03BA2DA");
                            Integer colorTo = Color.parseColor("#00000000");
                            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                                @Override
                                public void onAnimationUpdate(ValueAnimator animator) {
                                    selectedView.linearLayoutSelectedIndexIndicator.setBackgroundColor((Integer) animator.getAnimatedValue());
                                }

                            });
                            colorAnimation.setDuration(200);
                            colorAnimation.start();

                        }
                        Integer colorFrom = Color.parseColor("#00000000");
                        Integer colorTo = Color.parseColor("#C03BA2DA");
                        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animator) {
                                selectedView.linearLayoutSelectedIndexIndicator.setBackgroundColor((Integer) animator.getAnimatedValue());
                            }

                        });
                        colorAnimation.setDuration(200);
                        colorAnimation.start();

                        selectedView = thisViewHolder;
                        selectedIndex = thisViewHolder.position;
                        callBacks.updateSelection(taskData[thisViewHolder.position]);
                        return true; // stops sending signals to any other type of touch
                    } else {
                        callBacks.pointerUp(event.getX(), event.getY());
                    }

                    return true;
                }
            });
        }
    }

    // Returns the size of the taskData
    @Override
    public int getItemCount() {
        return taskData.length;
    }

    public void setSelectedItemIndex(int index) {
        callBacks.updateSelection(taskData[index]);
    }
}
