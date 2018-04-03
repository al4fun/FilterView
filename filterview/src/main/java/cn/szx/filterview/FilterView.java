package cn.szx.filterview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用方法：
 * --1.调用setXXX方法进行样式设置
 * --2.调用setData(List<ConditionGroup> groups, OnConfirmListener listener)方法进行初始化
 */
public class FilterView extends LinearLayout implements View.OnClickListener {
    long animateDuration = 300;//动画持续时间，ms
    int titleBackgroundColor = Color.parseColor("#ffffff");//标题栏背景色
    int titleTextColor = Color.parseColor("#333333");//标题栏文字颜色
    int contentBackgroundColor = Color.parseColor("#f6f6f6");//展开部分的背景色
    int buttonBackgroundDrawableResource = R.drawable.selector_filterview_btn;//确定按钮的背景
    int buttonTextColor = Color.parseColor("#ffffff");//确定按钮的文字颜色
    int groupTitleTextColor = Color.parseColor("#333333");//分组标题的文字颜色
    int conditionTextColorResource = R.color.selector_filterview_radio_text;//过滤条件单选框的文字颜色
    int conditionBackgroudDrawableResource = R.drawable.selector_filterview_radio_bg;//过滤条件单选框的背景

    float density;//屏幕像素密度
    LayoutInflater inflater;
    int contentHeight;//展开内容的高度

    RelativeLayout rl_title_root;
    TextView tv_title;
    ImageView iv_title_icon;
    LinearLayout ll_content_root, ll_content_container;
    Button btn_confirm;

    List<ConditionGroup> groups;
    OnConfirmListener listener;
    List<RvAdapter> adapters = new ArrayList<>();

    /**
     * 过滤条件组
     */
    public static class ConditionGroup {
        public String groupName;//组名
        public List<Condition> conditions;//过滤条件列表
        public String lastChosenConditionId;//上次选中的过滤条件的id
        public String chosenConditionId;//当前选中的过滤条件的id

        public static class Condition {
            public String conditionName;//过滤条件名
            public String conditionId;//过滤条件id
        }
    }

    public interface OnConfirmListener {
        void onConfirm(List<String> checkedConditionIds);
    }

    public FilterView(Context context) {
        this(context, null);
    }

    public FilterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        density = getContext().getResources().getDisplayMetrics().density;
        inflater = LayoutInflater.from(getContext());
    }

    public void setData(List<ConditionGroup> groups, OnConfirmListener listener) {
        this.groups = groups;
        this.listener = listener;

        init();
    }

    private void init() {
        View view = inflater.inflate(R.layout.view_filterview, this, true);
        rl_title_root = view.findViewById(R.id.rl_title_root);
        rl_title_root.setBackgroundColor(titleBackgroundColor);
        rl_title_root.setOnClickListener(this);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setTextColor(titleTextColor);
        iv_title_icon = findViewById(R.id.iv_title_icon);
        ll_content_root = view.findViewById(R.id.ll_content_root);
        ll_content_root.setBackgroundColor(contentBackgroundColor);
        ll_content_container = view.findViewById(R.id.ll_content_container);
        btn_confirm = view.findViewById(R.id.btn_confirm);
        btn_confirm.setBackgroundResource(buttonBackgroundDrawableResource);
        btn_confirm.setTextColor(buttonTextColor);
        btn_confirm.setOnClickListener(this);

        if (groups != null && groups.size() > 0) {
            //填充标题栏视图
            StringBuilder chosenConditions = new StringBuilder();
            for (ConditionGroup conditionGroup : groups) {
                for (ConditionGroup.Condition condition : conditionGroup.conditions) {
                    if (condition.conditionId.equals(conditionGroup.chosenConditionId)) {
                        if (chosenConditions.length() == 0) {
                            chosenConditions.append(condition.conditionName);
                        } else {
                            chosenConditions.append("/").append(condition.conditionName);
                        }
                        break;
                    }
                }
            }
            tv_title.setText(chosenConditions.toString());

            //填充内容视图
            for (ConditionGroup conditionGroup : groups) {
                View filterGroup = inflater.inflate(R.layout.view_filterview_group, ll_content_container, false);

                TextView tv_group_title = filterGroup.findViewById(R.id.tv_group_title);
                tv_group_title.setTextColor(groupTitleTextColor);
                tv_group_title.setText(conditionGroup.groupName);

                RecyclerView rv_group_content = filterGroup.findViewById(R.id.rv_group_content);
                rv_group_content.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
                RvAdapter rvAdapter = new RvAdapter(conditionGroup);
                adapters.add(rvAdapter);
                rv_group_content.setAdapter(rvAdapter);

                ll_content_container.addView(filterGroup);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (ll_content_root != null && contentHeight == 0) {//尚未初始化
            contentHeight = ll_content_root.getHeight();
            ll_content_root.setPadding(0, -contentHeight, 0, 0);//初始状态时，隐藏内容部分
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rl_title_root) {//点击标题栏
            if (ll_content_root.getPaddingTop() == 0) {//内容目前是显示状态
                animateHide();//隐藏内容
            } else {//内容目前是隐藏状态
                //更新数据与内容视图
                for (ConditionGroup group : groups) {
                    group.chosenConditionId = group.lastChosenConditionId;
                }
                for (RvAdapter adapter : adapters) {
                    adapter.notifyDataSetChanged();
                }

                animateShow();//显示内容
            }
        } else if (v.getId() == R.id.btn_confirm) {//点击内容部分的确定按钮
            animateHide();//隐藏内容

            List<String> ids = new ArrayList<>();//被选中的过滤条件的id
            boolean changed = false;//被选中的过滤条件是否有变化

            for (ConditionGroup group : groups) {
                if (!group.chosenConditionId.equals(group.lastChosenConditionId)) {
                    group.lastChosenConditionId = group.chosenConditionId;//覆写
                    changed = true;
                }
                ids.add(group.chosenConditionId);
            }

            //仅当被选中的过滤条件有变化时才更新标题栏和通知监听
            if (changed) {
                updateTitle();//更新标题栏
                listener.onConfirm(ids);
            }
        }
    }

    /**
     * 隐藏内容
     */
    private void animateHide() {
        ValueAnimator animator = ValueAnimator.ofInt(0, -contentHeight);
        animator.setDuration(animateDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (Integer) animation.getAnimatedValue();
                ll_content_root.setPadding(0, currentValue, 0, 0);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iv_title_icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.filter));
            }
        });

        animator.start();
    }

    /**
     * 显示内容
     */
    private void animateShow() {
        ValueAnimator animator = ValueAnimator.ofInt(-contentHeight, 0);
        animator.setDuration(animateDuration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (Integer) animation.getAnimatedValue();
                ll_content_root.setPadding(0, currentValue, 0, 0);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                iv_title_icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.arrow_up));
            }
        });

        animator.start();
    }

    /**
     * 更新标题栏的显示
     */
    private void updateTitle() {
        StringBuilder chosenConditions = new StringBuilder();
        for (ConditionGroup conditionGroup : groups) {
            for (ConditionGroup.Condition condition : conditionGroup.conditions) {
                if (condition.conditionId.equals(conditionGroup.chosenConditionId)) {
                    if (chosenConditions.length() == 0) {
                        chosenConditions.append(condition.conditionName);
                    } else {
                        chosenConditions.append("/").append(condition.conditionName);
                    }
                    break;
                }
            }
        }
        tv_title.setText(chosenConditions.toString());
    }

    class RvAdapter extends RecyclerView.Adapter {
        ConditionGroup conditionGroup;

        RvAdapter(ConditionGroup conditionGroup) {
            this.conditionGroup = conditionGroup;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setBackgroundResource(conditionBackgroudDrawableResource);
            radioButton.setButtonDrawable(null);
            radioButton.setGravity(Gravity.CENTER);
            radioButton.setTextColor(getResources().getColorStateList(conditionTextColorResource));
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_filterview_condition));
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(35));
            lp.setMargins(dp2px(10), dp2px(5), dp2px(10), dp2px(5));
            radioButton.setLayoutParams(lp);

            return new ItemViewHolder(radioButton);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            RadioButton rb = (RadioButton) itemViewHolder.view;
            rb.setText(conditionGroup.conditions.get(position).conditionName);
            if (conditionGroup.conditions.get(position).conditionId.equals(conditionGroup.chosenConditionId)) {
                rb.setChecked(true);
            } else {
                rb.setChecked(false);
            }

            //注：因为交互逻辑的关系，此处使用OnCheckChangeListener会造成并发notifyDataSetChanged异常
            rb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((RadioButton) v).isChecked()) {
                        refreshCheckedStatus(conditionGroup.conditions.get(position).conditionId);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return conditionGroup.conditions.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            View view;

            ItemViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }

        /**
         * 刷新group的选中状态
         */
        private void refreshCheckedStatus(String conditionId) {
            if (!conditionId.equals(conditionGroup.chosenConditionId)) {
                conditionGroup.chosenConditionId = conditionId;//刷新数据
                notifyDataSetChanged();//刷新视图
            }
        }
    }

    private int dp2px(int dp) {
        return (int) (dp * density);
    }

//setter--------------------------------------------------------------------------------------------

    public FilterView setAnimateDuration(long animateDuration) {
        this.animateDuration = animateDuration;
        return this;
    }

    public FilterView setTitleBackgroundColor(int color) {
        this.titleBackgroundColor = color;
        return this;
    }

    public FilterView setTitleTextColor(int color) {
        this.titleTextColor = color;
        return this;
    }

    public FilterView setContentBackgroundColor(int color) {
        this.contentBackgroundColor = color;
        return this;
    }

    public FilterView setButtonBackgroundDrawableResource(int drawableResource) {
        this.buttonBackgroundDrawableResource = drawableResource;
        return this;
    }

    public FilterView setButtonTextColor(int color) {
        this.buttonTextColor = color;
        return this;
    }

    public FilterView setGroupTitleTextColor(int color) {
        this.groupTitleTextColor = color;
        return this;
    }

    public FilterView setConditionTextColorResource(int colorResource) {
        this.conditionTextColorResource = colorResource;
        return this;
    }

    public FilterView setConditionBackgroudDrawableResource(int drawableResource) {
        this.conditionBackgroudDrawableResource = drawableResource;
        return this;
    }
}