package com.example.garrickw.splitpaneresearchanddevelopment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;

import com.example.garrickw.splitpaneresearchanddevelopment.adaptors.TaskListRecycleViewAdaptor;
import com.example.garrickw.splitpaneresearchanddevelopment.dummy.DummyContent;
import com.example.garrickw.splitpaneresearchanddevelopment.dummy.TaskModel;
import com.example.garrickw.splitpaneresearchanddevelopment.utils.MasterDetailAnimator;
import com.example.garrickw.splitpaneresearchanddevelopment.utils.Metrics;

public class TaskListActivity extends AppCompatActivity implements TaskListRecycleViewAdaptor.Callbacks, TaskListFragment.Callbacks {

    /*
     *  GUI CONFIGURATION
     */
    private final int MASTER_WIDTH_DP = 400;
    private final int DETAIL_TITLE_LEFT_PADDING_DP = 50;
    private final int ANIMATION_SPEED_MILLIS = 300;
    private final int MENU_CLOSED_DP = 58;
    private final int MENU_OPEN_DP = 200;

    private final int K2_PROGRESS_ANIMATION_SPEED_MULTIPLIER = 4;
    private final int K2_PROGRESS_ANIMATION_DELAY_MILLIS = 150;

    private boolean mTwoPane;
    private TaskListRecycleViewAdaptor mAdapter;

    private static final int SWIPE_MIN_DISTANCE = 0;
    private float downX, downY;
    private int menuWidth;
    private int detailWidth;
    private boolean menuOpen = false;
    private FloatingActionButton fab;
    private TaskModel selectedTaskModel;

    private CustomProgressDialogue customProgressDialogue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_app_bar);

        if (findViewById(R.id.task_detail_container) != null) {
            mTwoPane = true;
        }

        showLoadingScreenWithoutFadeIn();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //set layoutManger
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TaskListRecycleViewAdaptor(new DummyContent().getDummyData(), this);
        //set adapter
        recyclerView.setAdapter(mAdapter);
        //set item animator to DefaultAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View.OnClickListener onClickListenerForFab = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        });
                        showLoadingScreen();

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        fab.setOnClickListener(onClickListenerForFab);
                        hideLoadingScreen();
                    }
                }).start();
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mTwoPane) {
            //check to see if we are loaded as landscape, if so we load the first itme
//            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//            }

            updateGuiBasedOnSelections(); // update the view to the desired orientation configuration
        }

        findViewById(R.id.task_detail_instruction_imageBtn).setSelected(true); //TODO

        // add all the listeners
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchBar();
            }
        });
        findViewById(R.id.close_search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchBar();
            }
        });
        findViewById(R.id.open_menu_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuOpenClose();
            }
        });

        // make sure wew are only dealing with a two pane layout for this
        if (mTwoPane) {
            setupDetailDragListener();
            setupMenuDragListener();
        }
        setupMenuListeners();
        setupDetailListeners();
        evaluateDrag();

        hideLoadingScreen();
    }

    @Override
    public boolean updateDownPoint(float x, float y) {
        downX = x;
        downY = y;
        menuWidth = findViewById(R.id.menu_content_holder).getWidth();
        detailWidth = findViewById(R.id.task_list_holder).getWidth();
        return true;
    }

    @Override
    public boolean movePointer(float x, float y) {
        float deltaX = downX - x;
        float deltaY = downY - y;

        // swipe horizontal
        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
            // left or right
            moveMenu(((int) deltaX) * -1);
        }

        return true;
    }

    @Override
    public boolean movePointerIgnoreY(float x) {
        float deltaX = downX - x;
        moveMenu(((int) deltaX) * -1);
        return true;
    }

    @Override
    public boolean pointerUp(float x, float y) {
        float deltaX = downX - x;
        float deltaY = downY - y;

        // swipe horizontal
        if (Math.abs(deltaX) < 20 && Math.abs(deltaY) < 20) {
            // left or right
            moveMenu(((int) deltaX) * -1);
            evaluateDrag();
            return false;
        } else {
            evaluateDrag();
            return true;
        }
    }

    @Override
    public void updateSelection(TaskModel taskModel) {
        this.selectedTaskModel = taskModel;

        showLoadingScreen();

        // title
        TextSwitcher textSwitcherTitle = (TextSwitcher) findViewById(R.id.task_detail_title_text);
        textSwitcherTitle.setInAnimation(this, R.anim.up_from_bottom_in);
        textSwitcherTitle.setOutAnimation(this, R.anim.up_from_bottom_out);
        textSwitcherTitle.setText(taskModel.getName());

        // folio
        TextSwitcher textSwitcherFolio = (TextSwitcher) findViewById(R.id.task_detail_folio_text);
        textSwitcherFolio.setInAnimation(this, R.anim.slide_in_right_view);
        textSwitcherFolio.setOutAnimation(this, R.anim.slide_out_left_view);
        textSwitcherFolio.setText(taskModel.getFolio());

        // process
        TextSwitcher textSwitcherProcess = (TextSwitcher) findViewById(R.id.task_detail_process_name_text);
        textSwitcherProcess.setInAnimation(this, R.anim.slide_in_right_view);
        textSwitcherProcess.setOutAnimation(this, R.anim.slide_out_left_view);
        textSwitcherProcess.setText(taskModel.getProcess());

        if (mTwoPane) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                openDetail();
            }
        }

        hideLoadingScreen();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mTwoPane) {
            updateGuiBasedOnSelections();
            evaluateDrag();
        }
    }

    private void openDetail() {
        updateGuiBasedOnSelections();
    }

    private void setupDetailDragListener() {
        findViewById(R.id.rootParentView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return updateDownPoint(event.getRawX(), event.getRawY());
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return movePointer(event.getRawX(), event.getRawY());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    evaluateDrag();
                } else {
                    evaluateDrag();
                }

                return false;
            }
        });
    }

    private void setupMenuDragListener() {
        findViewById(R.id.menu_container_scroller).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    updateDownPoint(event.getRawX(), event.getRawY());
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return !movePointerIgnoreY(event.getRawX());
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    evaluateDrag();
                } else {
                    evaluateDrag();
                }

                return false;
            }
        });
    }

    private void showLoadingScreenWithoutFadeIn() {
        final Activity fActivity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setDuration(ANIMATION_SPEED_MILLIS);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.fab).setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                findViewById(R.id.fab).startAnimation(fadeOut);

                findViewById(R.id.loadingWindowContainerViewRelativeLayout).setVisibility(View.VISIBLE);

                if (customProgressDialogue == null) {
                    customProgressDialogue = new CustomProgressDialogue(fActivity,
                            (RelativeLayout) findViewById(R.id.progress_loader_animation));
                    customProgressDialogue.setDelay(K2_PROGRESS_ANIMATION_DELAY_MILLIS);
                    customProgressDialogue.setSpeed(K2_PROGRESS_ANIMATION_SPEED_MULTIPLIER);
                    customProgressDialogue.show();
                } else {
                    customProgressDialogue.setSpeed(K2_PROGRESS_ANIMATION_SPEED_MULTIPLIER);
                    customProgressDialogue.show();
                }
            }
        });
    }

    private void showLoadingScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingWindowContainerViewRelativeLayout).setVisibility(View.VISIBLE);

                // block all the touch events from the underlying layers
                findViewById(R.id.loadingWindowContainerViewRelativeLayout).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new AccelerateInterpolator()); //and this
                fadeIn.setDuration(ANIMATION_SPEED_MILLIS);
                findViewById(R.id.loadingWindowContainerViewRelativeLayout).startAnimation(fadeIn);

                showLoadingScreenWithoutFadeIn();
            }
        });
    }

    private void hideLoadingScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                customProgressDialogue.setSpeed(K2_PROGRESS_ANIMATION_SPEED_MULTIPLIER * 2);
                customProgressDialogue.dismiss();

                if (mTwoPane) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if(selectedTaskModel != null) {
                            fab.setVisibility(View.VISIBLE);
                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new AccelerateInterpolator());
                            fadeIn.setDuration(300);
                            fab.startAnimation(fadeIn);
                        }
                    } else {
                        fab.setVisibility(View.VISIBLE);
                        Animation fadeIn = new AlphaAnimation(0, 1);
                        fadeIn.setInterpolator(new AccelerateInterpolator());
                        fadeIn.setDuration(300);
                        fab.startAnimation(fadeIn);
                    }
                }
            }
        });
    }

    private void setupMenuListeners() {
        View.OnClickListener menuListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int amountToMove = Metrics.convertPixelsToDp(v.getTop(), getApplicationContext());

                final View animatedView = findViewById(R.id.menuSelectorIndicatorImageView);
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animatedView.getLayoutParams();
                ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, Metrics.convertDpToPixel(amountToMove, getApplicationContext()));
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                        animatedView.requestLayout();
                    }
                });
                animator.setDuration(ANIMATION_SPEED_MILLIS);
                animator.start();
            }
        };

        findViewById(R.id.menu_inbox_button_linearLayout).setOnClickListener(menuListener);
        findViewById(R.id.menu_outbox_button_linearLayout).setOnClickListener(menuListener);
        findViewById(R.id.menu_forms_button_linearLayout).setOnClickListener(menuListener);

        findViewById(R.id.menu_feedback_button_linearLayout).setOnClickListener(menuListener);
        findViewById(R.id.menu_logout_button_linearLayout).setOnClickListener(menuListener);

        // set the height of the selection indicator
        ImageView selectionIndicator = (ImageView) findViewById(R.id.menuSelectorIndicatorImageView);
        View view = findViewById(R.id.menu_inbox_button_linearLayout);
        view.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        selectionIndicator.getLayoutParams().height = findViewById(R.id.menu_inbox_button_linearLayout).getLayoutParams().height;
        selectionIndicator.requestLayout();
    }

    private void setupDetailListeners(){
        findViewById(R.id.task_detail_instruction_imageBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.task_detail_instruction_imageBtn).setSelected(true);
                fadeInDetailBackground(findViewById(R.id.task_detail_instruction_imageBtn));

                if(findViewById(R.id.task_detail_assigned_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_assigned_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_assigned_imageBtn));
                }
                if(findViewById(R.id.task_detail_time_card_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_time_card_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_time_card_imageBtn));
                }
            }
        });

        findViewById(R.id.task_detail_assigned_imageBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.task_detail_assigned_imageBtn).setSelected(true);
                fadeInDetailBackground(findViewById(R.id.task_detail_assigned_imageBtn));

                if (findViewById(R.id.task_detail_instruction_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_instruction_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_instruction_imageBtn));
                }
                if (findViewById(R.id.task_detail_time_card_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_time_card_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_time_card_imageBtn));
                }
            }
        });

        findViewById(R.id.task_detail_time_card_imageBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.task_detail_time_card_imageBtn).setSelected(true);
                fadeInDetailBackground(findViewById(R.id.task_detail_time_card_imageBtn));

                if (findViewById(R.id.task_detail_instruction_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_instruction_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_instruction_imageBtn));
                }
                if (findViewById(R.id.task_detail_assigned_imageBtn).isSelected()) {
                    findViewById(R.id.task_detail_assigned_imageBtn).setSelected(false);
                    fadeOutDetailBackground(findViewById(R.id.task_detail_assigned_imageBtn));
                }
            }
        });

        findViewById(R.id.close_detail_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTaskModel = null;
                updateGuiBasedOnSelections();
            }
        });
    }

    private void fadeOutDetailBackground(final View fadeOutView) {
        Integer colorFrom = getResources().getColor(R.color.custom_sleep_save_button_background);
        Integer colorTo = getResources().getColor(R.color.transparent);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                fadeOutView.setBackgroundColor((Integer)animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private void fadeInDetailBackground(final View fadeInView) {
        Integer colorFrom = getResources().getColor(R.color.transparent);
        Integer colorTo = getResources().getColor(R.color.custom_sleep_save_button_background);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                fadeInView.setBackgroundColor((Integer)animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private void evaluateDrag() {
        menuOpen = ((RelativeLayout.LayoutParams) findViewById(R.id.menu_content_holder).getLayoutParams()).width
                < Metrics.convertDpToPixel((MENU_OPEN_DP + MENU_CLOSED_DP) / 2, this);
        menuOpenClose();
    }

    private void moveMenu(int amount) {
        // check to see if we are two pane and portrait
        if (mTwoPane) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                findViewById(R.id.open_menu_button_text).setVisibility(View.VISIBLE);
                if (amount > 0) {
                    if (menuWidth + amount < Metrics.convertDpToPixel(MENU_OPEN_DP, this)) {
                        ((RelativeLayout.LayoutParams) findViewById(R.id.menu_content_holder).getLayoutParams()).width = menuWidth + amount;
                        ((RelativeLayout.LayoutParams) findViewById(R.id.task_list_holder).getLayoutParams()).width = detailWidth + amount;
                        findViewById(R.id.task_list_holder).requestLayout();
                    }
                } else {
                    if (menuWidth + amount > Metrics.convertDpToPixel(MENU_CLOSED_DP, this)) {
                        ((RelativeLayout.LayoutParams) findViewById(R.id.menu_content_holder).getLayoutParams()).width = menuWidth + amount;
                        ((RelativeLayout.LayoutParams) findViewById(R.id.task_list_holder).getLayoutParams()).width = detailWidth + amount;
                        findViewById(R.id.task_list_holder).requestLayout();
                    }
                }
            } else {
                if (amount > 0) {
                    if (menuWidth + amount < Metrics.convertDpToPixel(MENU_OPEN_DP, this)) {
                        ((RelativeLayout.LayoutParams) findViewById(R.id.menu_content_holder).getLayoutParams()).width = menuWidth + amount;
                        findViewById(R.id.task_list_holder).requestLayout();
                    }
                } else {
                    if (menuWidth + amount > Metrics.convertDpToPixel(MENU_CLOSED_DP, this)) {
                        ((RelativeLayout.LayoutParams) findViewById(R.id.menu_content_holder).getLayoutParams()).width = menuWidth + amount;
                        findViewById(R.id.task_list_holder).requestLayout();
                    }
                }
            }
        }
    }

    private void updateGuiBasedOnSelections() {
        // make sure wew are only dealing with a two pane layout for this
        if (mTwoPane) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                findViewById(R.id.divider_space).setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);

                if (!menuOpen) {
                    ((RelativeLayout.LayoutParams) findViewById(R.id.task_list_holder).getLayoutParams()).width = Metrics.convertDpToPixel(MASTER_WIDTH_DP, getApplicationContext());
                } else {
                    ((RelativeLayout.LayoutParams) findViewById(R.id.task_list_holder).getLayoutParams()).width = Metrics.convertDpToPixel(MASTER_WIDTH_DP + MENU_OPEN_DP - MENU_CLOSED_DP, getApplicationContext());
                }

                if (findViewById(R.id.close_detail_button) != null) {
                    findViewById(R.id.close_detail_button).setVisibility(View.GONE);
                }

                if(selectedTaskModel == null) {
                    mAdapter.setSelectedItemIndex(0);
                }
            } else {
                findViewById(R.id.divider_space).setVisibility(View.GONE);

                // check to see if anything is selected
                if (selectedTaskModel != null) {
                    fab.setVisibility(View.VISIBLE);

                    Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new AccelerateInterpolator()); //and this
                    fadeIn.setDuration(ANIMATION_SPEED_MILLIS);
                    fab.startAnimation(fadeIn);

                    // here we grow the size of the detail
                    MasterDetailAnimator mda = new MasterDetailAnimator(findViewById(R.id.task_list_holder), 0);
                    mda.setDuration(ANIMATION_SPEED_MILLIS);
                    findViewById(R.id.task_list_holder).startAnimation(mda);

                    if (findViewById(R.id.close_detail_button) != null) {
                        findViewById(R.id.close_detail_button).setVisibility(View.VISIBLE);
                        findViewById(R.id.task_detail_title_text).setPadding(Metrics.convertDpToPixel(DETAIL_TITLE_LEFT_PADDING_DP, getApplicationContext()), 0, 0, 0);
                    }
                } else {
                    Animation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                    fadeOut.setDuration(ANIMATION_SPEED_MILLIS);

                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            fab.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    fab.startAnimation(fadeOut);
                    // here we grow the size of the master
                    Display display = getWindowManager().getDefaultDisplay();
                    final Point size = new Point();
                    display.getSize(size);

                    if (((RelativeLayout.LayoutParams) findViewById(R.id.task_list_holder).getLayoutParams()).width != size.x) {
                        MasterDetailAnimator mda = new MasterDetailAnimator(findViewById(R.id.task_list_holder), size.x);
                        mda.setDuration(ANIMATION_SPEED_MILLIS);
                        findViewById(R.id.task_list_holder).startAnimation(mda);
                    }

                    if (findViewById(R.id.close_detail_button) != null) {
                        findViewById(R.id.close_detail_button).setVisibility(View.GONE);
                        findViewById(R.id.task_detail_title_text).setPadding(0, 0, 0, 0);

                    }
                }
            }
        }
    }

    private void showSearchBar() {
        // first we attend to the branding text
        findViewById(R.id.master_search_text).setVisibility(View.VISIBLE);
        findViewById(R.id.close_search_button).setVisibility(View.VISIBLE);

        findViewById(R.id.master_search_text).requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        Animation spin = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        spin.setDuration(ANIMATION_SPEED_MILLIS);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator()); //and this
        fadeIn.setDuration(ANIMATION_SPEED_MILLIS);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(ANIMATION_SPEED_MILLIS);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.search_button).setVisibility(View.INVISIBLE);
                findViewById(R.id.logo_mobile_text_image).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet animationSetForSearchButton = new AnimationSet(true);
        animationSetForSearchButton.addAnimation(spin);
        animationSetForSearchButton.addAnimation(fadeOut);

        AnimationSet animationSetForCloseButton = new AnimationSet(true);
        animationSetForCloseButton.addAnimation(spin);
        animationSetForCloseButton.addAnimation(fadeIn);


        findViewById(R.id.master_search_text).startAnimation(fadeIn);
        findViewById(R.id.logo_mobile_text_image).startAnimation(fadeOut);
        findViewById(R.id.search_button).startAnimation(animationSetForSearchButton);
        findViewById(R.id.close_search_button).startAnimation(animationSetForCloseButton);
    }

    private void hideSearchBar() {
        hideKeyboard();

        findViewById(R.id.search_button).setVisibility(View.VISIBLE);
        findViewById(R.id.logo_mobile_text_image).setVisibility(View.VISIBLE);

        Animation spin = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        spin.setDuration(ANIMATION_SPEED_MILLIS);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator()); //and this
        fadeIn.setDuration(ANIMATION_SPEED_MILLIS);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(ANIMATION_SPEED_MILLIS);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.close_search_button).setVisibility(View.GONE);
                findViewById(R.id.master_search_text).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AnimationSet animationSetForCloseButton = new AnimationSet(true);
        animationSetForCloseButton.addAnimation(spin);
        animationSetForCloseButton.addAnimation(fadeOut);

        AnimationSet animationSetForSearchButton = new AnimationSet(true);
        animationSetForSearchButton.addAnimation(spin);
        animationSetForSearchButton.addAnimation(fadeIn);

        findViewById(R.id.master_search_text).startAnimation(fadeOut);
        findViewById(R.id.logo_mobile_text_image).startAnimation(fadeIn);
        findViewById(R.id.search_button).startAnimation(animationSetForSearchButton);
        findViewById(R.id.close_search_button).startAnimation(animationSetForCloseButton);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void menuOpenClose() {
        if (menuOpen) { // then we must close the menu
            menuOpen = false;
            MasterDetailAnimator mda = new MasterDetailAnimator(findViewById(R.id.menu_content_holder), Metrics.convertDpToPixel(MENU_CLOSED_DP, this));
            mda.setDuration(ANIMATION_SPEED_MILLIS);
            findViewById(R.id.menu_content_holder).startAnimation(mda);

            if (mTwoPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MasterDetailAnimator mdaFormaster = new MasterDetailAnimator(findViewById(R.id.task_list_holder),
                        Metrics.convertDpToPixel(MASTER_WIDTH_DP, getApplicationContext()));
                mdaFormaster.setDuration(ANIMATION_SPEED_MILLIS);
                findViewById(R.id.task_list_holder).startAnimation(mdaFormaster);
            }

            if (findViewById(R.id.menu_user_details_holder).getVisibility() == View.VISIBLE) {
                findViewById(R.id.menu_user_details_holder).setVisibility(View.VISIBLE);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(ANIMATION_SPEED_MILLIS / 2);

                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.menu_user_details_holder).setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                findViewById(R.id.menu_user_details_holder).startAnimation(fadeOut);

                final View animatedView = findViewById(R.id.menu_user_details_holder);
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animatedView.getLayoutParams();
                ValueAnimator animator = ValueAnimator.ofInt(params.topMargin, findViewById(R.id.menu_user_details_holder).getHeight() * -1);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                        animatedView.requestLayout();
                    }
                });
                animator.setDuration(ANIMATION_SPEED_MILLIS);
                animator.start();


                final View mobileTextImageView = findViewById(R.id.logo_mobile_text_image);
                final RelativeLayout.LayoutParams mobileTextViewParams = (RelativeLayout.LayoutParams) mobileTextImageView.getLayoutParams();
                ValueAnimator mobileTextAnimator = ValueAnimator.ofInt(Metrics.convertDpToPixel(7, this), Metrics.convertDpToPixel(18, this));
                mobileTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        mobileTextViewParams.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                        mobileTextImageView.requestLayout();
                    }
                });
                mobileTextAnimator.setDuration(ANIMATION_SPEED_MILLIS);
                mobileTextAnimator.start();
            }
        } else { // we must open the menu
            menuOpen = true;
            MasterDetailAnimator mda = new MasterDetailAnimator(findViewById(R.id.menu_content_holder), Metrics.convertDpToPixel(MENU_OPEN_DP, this));
            mda.setDuration(ANIMATION_SPEED_MILLIS);
            findViewById(R.id.menu_content_holder).startAnimation(mda);

            if (mTwoPane && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                MasterDetailAnimator mdaForMaster = new MasterDetailAnimator(findViewById(R.id.task_list_holder),
                        Metrics.convertDpToPixel(MASTER_WIDTH_DP, this) + Metrics.convertDpToPixel(MENU_OPEN_DP - MENU_CLOSED_DP, this));

                mdaForMaster.setDuration(ANIMATION_SPEED_MILLIS);
                findViewById(R.id.task_list_holder).startAnimation(mdaForMaster);
            }

            if (findViewById(R.id.menu_user_details_holder).getVisibility() != View.VISIBLE) {
                findViewById(R.id.menu_user_details_holder).setVisibility(View.VISIBLE);

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new AccelerateInterpolator());
                fadeIn.setDuration(ANIMATION_SPEED_MILLIS);

                findViewById(R.id.menu_user_details_holder).startAnimation(fadeIn);

                final View animatedView = findViewById(R.id.menu_user_details_holder);
                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) animatedView.getLayoutParams();
                ValueAnimator animator = ValueAnimator.ofInt(findViewById(R.id.menu_user_details_holder).getHeight() * -1, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                        animatedView.requestLayout();
                    }
                });
                animator.setDuration(ANIMATION_SPEED_MILLIS);
                animator.start();

                final View mobileTextImageView = findViewById(R.id.logo_mobile_text_image);
                final RelativeLayout.LayoutParams mobileTextViewParams = (RelativeLayout.LayoutParams) mobileTextImageView.getLayoutParams();
                ValueAnimator mobileTextAnimator = ValueAnimator.ofInt(Metrics.convertDpToPixel(18, this), Metrics.convertDpToPixel(7, this));
                mobileTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        mobileTextViewParams.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                        mobileTextImageView.requestLayout();
                    }
                });
                mobileTextAnimator.setDuration(ANIMATION_SPEED_MILLIS);
                mobileTextAnimator.start();
            }
        }
    }

    @Override
    public void onItemSelected(int index) {
        // nothing yet
    }
}
