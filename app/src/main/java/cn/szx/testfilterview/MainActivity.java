package cn.szx.testfilterview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.szx.filterview.FilterView;

public class MainActivity extends AppCompatActivity {
    FilterView filterview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        filterview = findViewById(R.id.filterview);
        filterview.setData(produceData(), new FilterView.OnConfirmListener() {
            @Override
            public void onConfirm(List<String> chosenConditionIds) {
                StringBuilder sb = new StringBuilder("选中项id：");
                for (String id : chosenConditionIds) {
                    sb.append(id).append(",");
                }
                String str = sb.toString().substring(0, sb.length() - 1);//去除末尾多余分隔符

                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });

        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(new RvAdapter());
    }

    /**
     * 生成测试数据
     */
    private List<FilterView.ConditionGroup> produceData() {
        List<FilterView.ConditionGroup> groups = new ArrayList<>();
        String[] time = {"2014年", "2015年", "2016年", "2017年", "2018年"};
        String[] school = {"小学", "初中", "高中"};

        FilterView.ConditionGroup timeGroup = new FilterView.ConditionGroup();
        timeGroup.groupName = "统计时段";
        timeGroup.conditions = new ArrayList<>();
        for (int i = 0; i < time.length; i++) {
            FilterView.ConditionGroup.Condition condition = new FilterView.ConditionGroup.Condition();
            condition.conditionName = time[i];
            condition.conditionId = "时段" + (i + 1);
            timeGroup.conditions.add(condition);
        }
        timeGroup.lastChosenConditionId = "时段1";
        timeGroup.chosenConditionId = "时段1";
        groups.add(timeGroup);

        FilterView.ConditionGroup schoolGroup = new FilterView.ConditionGroup();
        schoolGroup.groupName = "统计类别";
        schoolGroup.conditions = new ArrayList<>();
        for (int i = 0; i < school.length; i++) {
            FilterView.ConditionGroup.Condition condition = new FilterView.ConditionGroup.Condition();
            condition.conditionName = school[i];
            condition.conditionId = "类别" + (i + 1);
            schoolGroup.conditions.add(condition);
        }
        schoolGroup.lastChosenConditionId = "类别1";
        schoolGroup.chosenConditionId = "类别1";
        groups.add(schoolGroup);

        return groups;
    }

    class RvAdapter extends RecyclerView.Adapter {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(inflater.inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((Holder) holder).tv.setText("列表项" + position);
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class Holder extends RecyclerView.ViewHolder {
            TextView tv;

            Holder(View itemView) {
                super(itemView);

                tv = itemView.findViewById(R.id.tv);
            }
        }
    }
}